package com.github.deeepamin.ciaid.services.listeners;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CIAidYamlAsyncListener implements AsyncFileListener {
  private static final Logger LOG = Logger.getInstance(CIAidYamlAsyncListener.class);
  @Override
  public @Nullable ChangeApplier prepareChange(@NotNull List<? extends @NotNull VFileEvent> events) {
    for (VFileEvent event : events) {
      var file = event.getFile();
      var isGitlabCIYaml = CIAidProjectService.isValidGitlabCIYamlFile(file);
      if (!isGitlabCIYaml) {
        return null;
      }
      var project = ProjectLocator.getInstance().guessProjectForFile(file);
      if (project == null) {
        LOG.debug("Couldn't find project for changed file " + file.getPath());
        return null;
      }
      var projectService = CIAidProjectService.getInstance(project);
      if (event instanceof VFileContentChangeEvent) {
        return new ChangeApplier() {
          @Override
          public void afterVfsChange() {
            boolean userMarked = CIAidProjectService.isMarkedAsUserCIYamlFile(file);
            projectService.readGitlabCIYamlData(file, userMarked, false);
            var psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
              DaemonCodeAnalyzer.getInstance(project).restart(psiFile, "GitLab CI YAML changed, re-analyzing");
            }
          }
        };
      }
      if (event instanceof VFileDeleteEvent) {
        return new ChangeApplier() {
          @Override
          public void afterVfsChange() {
            projectService.getPluginData().remove(file);
          }
        };
      }
    }
    return null;
  }
}
