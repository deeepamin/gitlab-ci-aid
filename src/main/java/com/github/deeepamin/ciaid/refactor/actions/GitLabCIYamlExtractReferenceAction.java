package com.github.deeepamin.ciaid.refactor.actions;

import com.github.deeepamin.ciaid.refactor.dialogs.ExtractReferenceDialog;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;

public class GitLabCIYamlExtractReferenceAction extends BaseRefactorAction {
  @Override
  public void performRefactorAction(@NotNull AnActionEvent e) {
    var project = e.getProject();
    var editor = e.getData(CommonDataKeys.EDITOR);
    var element = e.getData(CommonDataKeys.PSI_ELEMENT);
    if (project == null || editor == null || !(element instanceof YAMLKeyValue keyValue)) {
      return;
    }
    var keyName = keyValue.getKeyText();
    var ciAidProjectService = CIAidProjectService.getCIAidProjectService(element);
    var files = ciAidProjectService.getPluginData()
            .keySet()
            .stream()
            .toList();
    var extractRefDialog = new ExtractReferenceDialog(project, keyName, files);
    if (extractRefDialog.showAndGet()) {
      extractReference(element, keyValue, extractRefDialog);
    }
  }

  private void extractReference(PsiElement element, YAMLKeyValue keyValue, ExtractReferenceDialog extractRefDialog) {
    if (element == null || keyValue.getValue() == null) {
      return;
    }
    var project = element.getProject();
    var selectedFilePath = extractRefDialog.getSelectedFilePath();
    var virtualFile = FileUtils.getVirtualFile(selectedFilePath, project).orElse(null);
    if (virtualFile == null) {
      return;
    }
    var rootMapping = YamlUtils.getRootMapping(project, selectedFilePath);
    if (rootMapping == null) {
      return;
    }
    WriteCommandAction.runWriteCommandAction(project, () -> {
      var keyName = keyValue.getKeyText();
      var referenceKey = extractRefDialog.getReferenceKey();
      var valueText = keyValue.getValue().getText();

      var generator = YAMLElementGenerator.getInstance(project);

      var extractedKeyValue = generator.createYamlKeyValue(keyName, valueText);
      var extractedMapping = createYamlMapping(project, extractedKeyValue);
      var referenceKeyValue = generator.createYamlKeyValue(referenceKey, extractedMapping.getText());

      var lastChild = rootMapping.getLastChild();
      var newLine = YAMLElementGenerator.getInstance(project).createEol();
      rootMapping.add(newLine);
      if (lastChild != null) {
        rootMapping.addAfter(newLine, lastChild);
      }
      rootMapping.add(referenceKeyValue);

      var replacement = createYamlKeyValue(project, keyName, REFERENCE_TAG + " [ " + referenceKey + ", " + keyName + " ]");
      if (replacement != null) {
        keyValue.replace(replacement);
      }

      var showOpenFileInEditor = extractRefDialog.shouldOpenFileInEditor();
      if (showOpenFileInEditor) {
        FileEditorManager.getInstance(project).openFile(virtualFile, true);
      }
    });
  }

  public boolean isAvailable(PsiElement element) {
    var file = element.getContainingFile();
    if (!(file instanceof YAMLFile)) {
      return false;
    }
    var isYamlFile = CIAidProjectService.isValidGitlabCIYamlFile(file.getVirtualFile());
    if (isYamlFile) {
      var topLevelKeys = YAMLUtil.getTopLevelKeys((YAMLFile) element.getContainingFile());
      var isYamlKeyValue = element instanceof YAMLKeyValue;
      if (!(isYamlKeyValue)) {
        return false;
      }
      var keyValue = (YAMLKeyValue) element;
      var isTopLevelKey = topLevelKeys.stream()
              .anyMatch(topLevelKeyValue -> topLevelKeyValue.getKeyText().equals(keyValue.getKeyText()));
      return !isTopLevelKey;
    }
    return false;
  }
}
