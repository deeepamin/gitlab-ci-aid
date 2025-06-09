package com.github.deeepamin.ciaid.refactor.moveHandlers;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.refactoring.move.MoveHandlerDelegate;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLAlias;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

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

  private void moveJob(PsiElement element, YAMLKeyValue jobToMove, String selectedFilePath, boolean showOpenFileInEditor) {
    var project = element.getProject();
    var virtualFile = FileUtils.getVirtualFile(selectedFilePath, project).orElse(null);
    if (virtualFile == null) {
      return;
    }
    var psiFile = PsiManager.getInstance(project).findFile(virtualFile);
    if (!(psiFile instanceof YAMLFile yamlFile)) {
      return;
    }
    var documents = yamlFile.getDocuments();
    if (documents == null || documents.isEmpty()) {
      return;
    }
    var topLevelValue = documents.getFirst().getTopLevelValue();
    if (!(topLevelValue instanceof YAMLMapping rootMapping)) {
      return;
    }
    WriteCommandAction.runWriteCommandAction(project, () -> {
      // check for anchors and aliases
      removeAnchors(jobToMove);
      expandAliases(jobToMove);

      var jobToMoveCopy = (YAMLKeyValue) jobToMove.copy();
      var lastChild = rootMapping.getLastChild();
      rootMapping.putKeyValue(jobToMoveCopy);

      var newLine = YAMLElementGenerator.getInstance(project).createEol();
      rootMapping.add(newLine);
      if (lastChild != null) {
        rootMapping.addAfter(newLine, lastChild);
      }
      var targetPsiFileDocument = PsiDocumentManager.getInstance(project).getDocument(psiFile);
      if (targetPsiFileDocument != null) {
        PsiDocumentManager.getInstance(project)
                .doPostponedOperationsAndUnblockDocument(targetPsiFileDocument);
      }

      removeEol(jobToMove);
      jobToMove.delete();

      CodeStyleManager.getInstance(project).reformat(psiFile);
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

  private void removeAnchors(YAMLKeyValue jobToMove) {
    if (jobToMove == null) {
      return;
    }
    var anchorElements = PsiUtils.findChildren(jobToMove, YAMLAnchor.class);
    for (YAMLAnchor anchor : anchorElements) {
      anchor.delete();
    }
  }

  private void expandAliases(YAMLKeyValue jobToMove) {
    if (jobToMove == null) {
      return;
    }
    var aliasElements = PsiUtils.findChildren(jobToMove, YAMLAlias.class);
    for (YAMLAlias alias : aliasElements) {
      var parent = alias.getParent();
      if (parent instanceof YAMLKeyValue keyValue) {
        var name = alias.getAliasName();
        var anchor = getAnchor(jobToMove, name);
        if (anchor == null || anchor.getMarkedValue() == null) {
          continue;
        }

        // copy to avoid modifying the original anchor
        var anchorValue = (YAMLValue) anchor.getMarkedValue().copy();
        var children = anchorValue.getChildren();
        for (PsiElement child : children) {
          if (child instanceof YAMLAnchor anchorChild) {
            removeEol(anchorChild);
            anchorChild.delete();
          }
        }
        var parentKeyText = keyValue.getKeyText();
        if (parentKeyText.equals("<<")) {
          var superParent = keyValue.getParent();
          if (superParent instanceof YAMLMapping superMapping) {
            if (anchorValue instanceof YAMLMapping anchorMapping) {
              removeEol(parent);
              parent.delete();
              anchorMapping.getKeyValues().forEach(superMapping::putKeyValue);
            }
          }
        } else {
          alias.delete();
          keyValue.setValue(anchorValue);
        }
      }
    }
  }

  private YAMLAnchor getAnchor(YAMLKeyValue jobToMove, String name) {
    if (jobToMove == null) {
      return null;
    }
    var project = jobToMove.getProject();
    return CIAidProjectService.getInstance(project)
            .getPluginData()
            .values()
            .stream()
            .flatMap(data -> data.getAnchors().stream())
            .filter(a -> a.getName().equals(name))
            .findFirst()
            .orElse(null);
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
        return jobs.contains(keyValue);
      }
    }
    return false;
  }

  private void removeEol(PsiElement element) {
    var nextSibling = element.getNextSibling();
    if (nextSibling != null && nextSibling.getText().equals("\n")) {
      element.getNextSibling().delete();
    }
  }

}

