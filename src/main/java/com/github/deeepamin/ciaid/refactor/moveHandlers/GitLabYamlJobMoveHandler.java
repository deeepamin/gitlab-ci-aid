package com.github.deeepamin.ciaid.refactor.moveHandlers;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.move.MoveHandlerDelegate;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.Map;

public class GitLabYamlJobMoveHandler extends MoveHandlerDelegate {
  @Override
  public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable PsiReference reference) {
    if (elements.length == 1) {
      return isAvailable(elements[0]);
    }
    return false;
  }

  @Override
  public boolean tryToMove(PsiElement element, Project project, DataContext dataContext, @Nullable PsiReference reference, Editor editor) {
    if (!isAvailable(element)) {
      return false;
    }
    var parent = element.getParent();
    if (parent instanceof YAMLKeyValue keyValue) {
      var ciAidProjectService = CIAidProjectService.getCIAidProjectService(element);
      var filteredFiles = ciAidProjectService.getPluginData()
              .entrySet()
              .stream()
              .filter((entry) -> {
                var data = entry.getValue();
                return !data.getJobElements().contains(keyValue);
              })
              .map(Map.Entry::getKey)
              .toList();

      var jobName = keyValue.getKeyText();
      var moveJobDialog = new MoveJobDialog(project, jobName, filteredFiles);
      if (moveJobDialog.showAndGet()) {
        var selectedFilePath = moveJobDialog.getSelectedFilePath();
        var showOpenFileInEditor = moveJobDialog.shouldOpenFileInEditor();
        moveJob(element, keyValue, selectedFilePath, showOpenFileInEditor);
      }
      return true;
    }
    return false;
  }

  private void moveJob(PsiElement element, YAMLKeyValue keyValue, String selectedFilePath, boolean showOpenFileInEditor) {
    var project = element.getProject();
    var virtualFile = FileUtils.getVirtualFile(selectedFilePath, project).orElse(null);
    if (virtualFile == null) {
      return;
    }
    var psiFile = PsiManager.getInstance(project).findFile(virtualFile);
    if (psiFile instanceof YAMLFile yamlFile) {
      var document = yamlFile.getDocuments().getFirst();
      var topLevelValue = document.getTopLevelValue();
      if (topLevelValue instanceof YAMLMapping rootMapping) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
          var keyValueCopy = (YAMLKeyValue) keyValue.copy();
          var lastChild = rootMapping.getLastChild();
          rootMapping.putKeyValue(keyValueCopy);
          var newLine = YAMLElementGenerator.getInstance(project).createEol();
          rootMapping.add(newLine);
          if (lastChild != null) {
            rootMapping.addAfter(newLine, lastChild);
          }
          keyValue.delete();
          var virtualFileDocument = FileDocumentManager.getInstance().getDocument(virtualFile);
          if (virtualFileDocument != null) {
            FileDocumentManager.getInstance().saveDocument(virtualFileDocument);
          }
          virtualFile.refresh(true, true);

          if (showOpenFileInEditor) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
          }
        });
      }
    }
  }

  private boolean isAvailable(PsiElement element) {
    var isGitLabCIYaml = CIAidProjectService.hasGitlabYamlFile(element);
    if (isGitLabCIYaml) {
      // job name as scalar when right click refactor on job, so get parent
      var parent = element.getParent();
      if (parent instanceof YAMLKeyValue keyValue) {
        var ciAidProjectService = CIAidProjectService.getCIAidProjectService(element);
        var jobs = ciAidProjectService.getPluginData()
                .values()
                .stream()
                .flatMap(data -> data.getJobElements().stream())
                .toList();
        var isJob = jobs.contains(keyValue);
        if (isJob) {
          // check for anchors and aliases

        }
      }
    }
    return false;
  }

}

