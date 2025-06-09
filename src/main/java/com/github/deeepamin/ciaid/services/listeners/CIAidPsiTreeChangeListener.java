package com.github.deeepamin.ciaid.services.listeners;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
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
      var ciAidProjectService = CIAidProjectService.getInstance(project);ciAidProjectService.getDataProvider()
              .readGitlabCIYamlData(virtualFile, CIAidProjectService.isMarkedAsUserCIYamlFile(virtualFile), true);
      DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
    });
  }
}
