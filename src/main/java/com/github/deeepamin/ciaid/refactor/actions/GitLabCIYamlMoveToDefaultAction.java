package com.github.deeepamin.ciaid.refactor.actions;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLSequence;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;

public class GitLabCIYamlMoveToDefaultAction extends AnAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getProject();
    var element = e.getData(CommonDataKeys.PSI_ELEMENT);
    if (project == null || !isAvailable(element)) {
      return;
    }
    var isGitLabCIYaml = CIAidProjectService.hasGitlabYamlFile(element);
    if (!isGitLabCIYaml) {
      return;
    }

    WriteCommandAction.runWriteCommandAction(project, () -> {
      var keyValue = (YAMLKeyValue) element;
      var file = (YAMLFile) element.getContainingFile();

      var topLevelValue = file.getDocuments().getFirst().getTopLevelValue();
      if (!(topLevelValue instanceof YAMLMapping rootMapping)) {
        return;
      }
      var defaultKeyValue = rootMapping.getKeyValueByKey(DEFAULT);
      if (defaultKeyValue == null) {
        // if the default section does not exist, create it
        var newDefaultKey = YAMLElementGenerator.getInstance(project).createYamlKeyValue(DEFAULT, "");
        var defaultMapping = createYamlMapping(project, keyValue);
        if (defaultMapping == null) {
          return;
        }
        // set the new default section with the being moved key
        newDefaultKey.setValue(defaultMapping);
        defaultKeyValue = (YAMLKeyValue) newDefaultKey.copy();
        insertKeyAtTop(rootMapping, defaultKeyValue);
      } else {
        // check if default section already contains the key
        if (defaultKeyValue.getValue() instanceof YAMLMapping existingMapping) {
          var keyText = keyValue.getKeyText();
          var keyExists = existingMapping.getKeyValues()
                  .stream()
                  .anyMatch(kv -> kv.getKeyText().equals(keyText));
          if (keyExists) {
            // show error if the key already exists in the default section
            CommonRefactoringUtil.showErrorHint(
                    project,
                    e.getData(CommonDataKeys.EDITOR),
                    CIAidBundle.message("refactoring.key-value.already.exists.in.default", keyValue.getKeyText()),
                    CIAidBundle.message("refactoring.cannot.refactor"),
                    null);
            return;
          }
          // otherwise, move the key to the default section
          var nextSibling = defaultKeyValue.getNextSibling();
          var newLineAtEnd = false;
          if (nextSibling != null) {
            var tokenType = nextSibling.getNode().getElementType();
            newLineAtEnd = tokenType != YAMLTokenTypes.EOL;
          }
          keyValue = getNewKeyValue(project, keyValue, newLineAtEnd);
        }
      }
      if (!(defaultKeyValue.getValue() instanceof YAMLMapping defaultMapping)) {
        return;
      }
      defaultMapping.putKeyValue(keyValue);
      var parentMapping = ((YAMLKeyValue) element).getParentMapping();
      element.delete();

      // check if the job is empty after moving the key to default
      if (parentMapping != null) {
        var isParentJobEmpty = parentMapping.getKeyValues().isEmpty();
        if (isParentJobEmpty) {
          var parentJob = parentMapping.getParent();
          if (!(parentJob instanceof YAMLKeyValue jobKeyValue)) {
            return;
          }
          jobKeyValue.delete();
        }
      }
    });
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
    e.getPresentation().setEnabledAndVisible(isAvailable(element));
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private boolean isAvailable(PsiElement element) {
    if (!(element instanceof YAMLKeyValue keyValue)) {
      return  false;
    }
    var isChildOfDefault = PsiUtils.isChild(element, List.of(DEFAULT));
    if (isChildOfDefault) {
      return false;
    }
    return DEFAULT_ALLOWED_KEYWORDS.contains(keyValue.getKeyText());
  }

  private static YAMLMapping createYamlMapping(Project project, YAMLKeyValue keyValue) {
    var keyValueText = keyValue.getText();
    var dummyFile = (YAMLFile) PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.yaml", YAMLFileType.YML, keyValueText);

    var dummyDoc = dummyFile.getDocuments().getFirst();
    if (dummyDoc == null || !(dummyDoc.getTopLevelValue() instanceof YAMLMapping yamlMapping)) {
      return null;
    }
    return yamlMapping;
  }

  private static void insertKeyAtTop(YAMLMapping rootMapping, YAMLKeyValue newKeyValue) {
    var existingKeys = rootMapping.getKeyValues().stream().toList();
    if (!existingKeys.isEmpty()) {
      var firstChild = rootMapping.getFirstChild();
      var newLine = YAMLElementGenerator.getInstance(rootMapping.getProject()).createEol();
      var inserted = rootMapping.addBefore(newKeyValue, firstChild);
      rootMapping.addBefore(newLine, firstChild);
      if (inserted != null) {
        rootMapping.addAfter(newLine, inserted);
      }
    } else {
      rootMapping.add(newKeyValue);
    }
  }

  private YAMLKeyValue getNewKeyValue(Project project, YAMLKeyValue keyValue, boolean newLineAtEnd) {
    var value = keyValue.getValue();
    var keyText = keyValue.getKeyText();
    if (value == null) {
      return keyValue;
    }
    String newKeyValueText;
    if (value instanceof YAMLSequence sequence) {
      newKeyValueText = sequence.getItems().stream()
              .map(item -> "- " + item.getText().replaceFirst("^-\\s*", "").trim())
              .collect(Collectors.joining("\n"));
    } else {
      newKeyValueText = value.getText().trim();
    }
    return YAMLElementGenerator.getInstance(project).createYamlKeyValue(keyText, newKeyValueText + (newLineAtEnd ? "\n" : ""));
  }
}
