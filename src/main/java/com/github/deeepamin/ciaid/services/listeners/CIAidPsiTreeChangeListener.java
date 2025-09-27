package com.github.deeepamin.ciaid.services.listeners;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import org.jetbrains.annotations.NotNull;

public class CIAidPsiTreeChangeListener extends PsiTreeChangeAdapter {
  private final Project project;

  public CIAidPsiTreeChangeListener(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
    var psiFile = event.getFile();
    if (psiFile == null || !CIAidProjectService.hasGitlabYamlFile(psiFile)) {
      return;
    }

    var virtualFile = psiFile.getVirtualFile();
    if (virtualFile == null) {
      return;
    }
    PsiDocumentManager.getInstance(project).performLaterWhenAllCommitted(() -> {
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        // Execute heavy operations on background thread
        var ciAidProjectService = CIAidProjectService.getInstance(project);
        ciAidProjectService.readGitlabCIYamlData(virtualFile, CIAidProjectService.isMarkedAsUserCIYamlFile(virtualFile), true);

        // Switch back to EDT for UI operations
        ApplicationManager.getApplication().invokeLater(() -> {
          DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
        });
      });
    });
  }
}
