package com.github.deeepamin.ciaid.refactor.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameHandlerRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

public class GitLabCIYamlExtractReferenceAction extends BaseRefactorAction {
  @Override
  public void performRefactorAction(@NotNull AnActionEvent e) {
    var project = e.getProject();
    var editor = e.getData(CommonDataKeys.EDITOR);
    var element = e.getData(CommonDataKeys.PSI_ELEMENT);
    if (project == null || editor == null || element == null) {
      // TODO show error message
      return;
    }

    var extractedReference = WriteCommandAction.writeCommandAction(project)
            .compute(() -> {
      var keyValue = (YAMLKeyValue) element;
      if (keyValue.getValue() == null) {
        return null;
      }
      var keyName = keyValue.getKeyText();
      var valueText = keyValue.getValue().getText();
      var referenceName = ".extracted";
      var file = (YAMLFile) element.getContainingFile();

      var topLevelValue = file.getDocuments().getFirst().getTopLevelValue();
      if (!(topLevelValue instanceof YAMLMapping rootMapping)) {
        return null;
      }
      var generator = YAMLElementGenerator.getInstance(project);

      var extractedKeyValue = generator.createYamlKeyValue(keyName, valueText);
      var extractedMapping = createYamlMapping(project, extractedKeyValue);
      var referenceKeyValue = generator.createYamlKeyValue(referenceName, extractedMapping.getText());

      var lastChild = rootMapping.getLastChild();
      var newLine = YAMLElementGenerator.getInstance(project).createEol();
      rootMapping.add(newLine);
      if (lastChild != null) {
        rootMapping.addAfter(newLine, lastChild);
      }

      var extracted = (YAMLKeyValue) rootMapping.add(referenceKeyValue);

      var replacement = createYamlKeyValue(project, keyName, "!reference [ " + referenceName + ", " + keyName + " ]");
      if (replacement != null) {
        keyValue.replace(replacement);
      }

      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

      var extractedKey = extracted.getKey();
      if (extractedKey != null) {
        editor.getCaretModel().moveToOffset(extractedKey.getTextOffset());
        editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
      }
      return extracted;
    });

    ApplicationManager.getApplication().invokeLater(() -> startInplaceRename(extractedReference, editor));
  }

  private void startInplaceRename(YAMLKeyValue extractedReference, Editor editor) {
    if (extractedReference == null) {
      return;
      // TODO show error message if the key is null
    }
    var dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
    var renameHandler = RenameHandlerRegistry.getInstance()
            .getRenameHandler(dataContext);

    if (renameHandler != null) {
      renameHandler.invoke(extractedReference.getProject(), new PsiElement[] { extractedReference }, dataContext);
    }
  }

  public boolean isAvailable(PsiElement element) {
    return element instanceof YAMLKeyValue;
  }
}
