package com.github.deeepamin.gitlabciaid.services.providers;

import com.github.deeepamin.gitlabciaid.services.GitlabCIYamlProjectService;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.AFTER_SCRIPT;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.BEFORE_SCRIPT;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.SCRIPT;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGES;
import static com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils.isYamlFile;

public class EditorNotificationProvider implements com.intellij.ui.EditorNotificationProvider {
  public static final Key<Boolean> GITLAB_CI_YAML_MARKED_KEY = Key.create("GitLabCI.YAML");
  private static final List<String> POTENTIAL_GITLAB_CI_ELEMENTS = List.of(STAGES, AFTER_SCRIPT, BEFORE_SCRIPT, SCRIPT, INCLUDE, STAGE);

  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
    return fileEditor -> {
      var projectService = GitlabCIYamlProjectService.getInstance(project);
      if (file.getUserData(GITLAB_CI_YAML_MARKED_KEY) != null || projectService.getPluginData().containsKey(file)) {
        // already read/marked file: true/false
        return null;
      }
      if (!isPotentialGitlabCIYamlFile(file, project)) {
        return null;
      }
      return getEditorNotificationPanel(project, file, projectService);
    };
  }

  private static @NotNull EditorNotificationPanel getEditorNotificationPanel(@NotNull Project project, @NotNull VirtualFile file, GitlabCIYamlProjectService projectService) {
    EditorNotificationPanel panel = new EditorNotificationPanel();
    panel.setText("Do you want to mark this file as a GitLab CI YAML file?");
    panel.createActionLabel("Mark as GitLab CI", () -> {
      projectService.readGitlabCIYamlData(project, file);
      EditorNotifications.getInstance(project).updateNotifications(file);
      ApplicationManager.getApplication().runWriteAction(() -> {
        var document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
          FileDocumentManager.getInstance().saveDocument(document);
          PsiManager.getInstance(project).dropPsiCaches();
        }
      });
    });
    //TODO ignore into excluded mappings so user can later remove from excluded if they marked by mistake / change mind
    panel.createActionLabel("Ignore", () -> file.putUserData(GITLAB_CI_YAML_MARKED_KEY, false));
    return panel;
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }

  private boolean isPotentialGitlabCIYamlFile(@NotNull VirtualFile file, Project project) {
    if (!isYamlFile(file)) {
      return false;
    }
    var psiFile = PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof YAMLFile)) {
      return false;
    }
    for (var potentialGitlabCiElement : POTENTIAL_GITLAB_CI_ELEMENTS) {
      if (PsiUtils.hasChild(psiFile, potentialGitlabCiElement)) {
        return true;
      }
    }
    return false;
  }
}
