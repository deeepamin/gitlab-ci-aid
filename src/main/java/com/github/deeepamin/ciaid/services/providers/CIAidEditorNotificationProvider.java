package com.github.deeepamin.ciaid.services.providers;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;

import javax.swing.JComponent;
import java.util.List;
import java.util.function.Function;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.services.CIAidProjectService.GITLAB_CI_YAML_USER_MARKED_KEY;
import static com.github.deeepamin.ciaid.utils.YamlUtils.isYamlFile;

public class CIAidEditorNotificationProvider implements com.intellij.ui.EditorNotificationProvider {
  private static final List<String> POTENTIAL_GITLAB_CI_ELEMENTS = List.of(STAGES, AFTER_SCRIPT, BEFORE_SCRIPT, SCRIPT, INCLUDE, STAGE, VARIABLES, WORKFLOW, COMPONENT, EXTENDS);
  private static final List<String> NON_GITLAB_CI_ELEMENTS = List.of("kind");

  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
    return fileEditor -> {
      var isEditorNotificationDisabled = CIAidSettingsState.getInstance(project).isEditorNotificationDisabled();
      if (isEditorNotificationDisabled) {
        return null;
      }
      var projectService = CIAidProjectService.getInstance(project);
      if (file.getUserData(GITLAB_CI_YAML_USER_MARKED_KEY) != null || projectService.getPluginData().containsKey(file)) {
        // already read/marked file: true/false
        return null;
      }
      if (!isPotentialGitlabCIYamlFile(file, project)) {
        return null;
      }
      return getEditorNotificationPanel(project, file, projectService);
    };
  }

  private static @NotNull EditorNotificationPanel getEditorNotificationPanel(@NotNull Project project, @NotNull VirtualFile file, CIAidProjectService projectService) {
    EditorNotificationPanel panel = new EditorNotificationPanel();
    panel.setText(CIAidBundle.message("editor.notification.mark-as-gitlab-yaml-question"));
    panel.createActionLabel(CIAidBundle.message("editor.notification.mark-as-gitlab-yaml"), () -> {
      CIAidProjectService.markAsUserCIYamlFile(file, project);
      projectService.readGitlabCIYamlData(file, true, false);
      EditorNotifications.getInstance(project).updateNotifications(file);
      ApplicationManager.getApplication().runWriteAction(() -> {
        var document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
          FileDocumentManager.getInstance().saveDocument(document);
          PsiManager.getInstance(project).dropPsiCaches();
        }
      });
    });
    panel.createActionLabel(CIAidBundle.message("editor.notification.ignore"), () -> {
      CIAidProjectService.ignoreCIYamlFile(file, project);
      EditorNotifications.getInstance(project).updateNotifications(file);
    });
    panel.createActionLabel(CIAidBundle.message("editor.notification.disable"), () -> {
      CIAidSettingsState.getInstance(project).setEditorNotificationDisabled(true);
      EditorNotifications.getInstance(project).updateNotifications(file);
    });
    return panel;
  }

  @Override
  public boolean isDumbAware() {
    return false;
  }

  private boolean isPotentialGitlabCIYamlFile(@NotNull VirtualFile file, Project project) {
    if (!isYamlFile(file)) {
      return false;
    }
    var psiFile = PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof YAMLFile)) {
      return false;
    }
    for (var nonGitlabCiElement : NON_GITLAB_CI_ELEMENTS) {
      if (PsiUtils.hasChild(psiFile, nonGitlabCiElement)) {
        return false;
      }
    }
    for (var potentialGitlabCiElement : POTENTIAL_GITLAB_CI_ELEMENTS) {
      if (PsiUtils.hasChild(psiFile, potentialGitlabCiElement)) {
        return true;
      }
    }
    return false;
  }
}
