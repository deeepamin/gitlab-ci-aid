package com.github.deeepamin.ciaid.services.providers;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.services.CIAidProjectService.GITLAB_CI_YAML_POTENTIAL_KEY;
import static com.github.deeepamin.ciaid.services.CIAidProjectService.GITLAB_CI_YAML_USER_MARKED_KEY;
import static com.github.deeepamin.ciaid.utils.YamlUtils.isYamlFile;

public class CIAidEditorNotificationProvider implements com.intellij.ui.EditorNotificationProvider {
  private static final Logger LOG = Logger.getInstance(CIAidEditorNotificationProvider.class);
  private static final List<String> POTENTIAL_GITLAB_CI_ELEMENTS = List.of(STAGES, AFTER_SCRIPT, BEFORE_SCRIPT, SCRIPT, INCLUDE, STAGE, VARIABLES, WORKFLOW, COMPONENT, EXTENDS);
  private static final Pattern YAML_KEY_PATTERN = Pattern.compile("(?m)^\\s*(?:-\\s*)?[\"']?([^\\s\"']+)[\"']?\\s*:");

  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
    final var isEditorNotificationDisabled = CIAidSettingsState.getInstance(project).isEditorNotificationDisabled();
    if (isEditorNotificationDisabled) {
      return null;
    }
    final var projectService = CIAidProjectService.getInstance(project);
    if (file.getUserData(GITLAB_CI_YAML_USER_MARKED_KEY) != null || projectService.getPluginData().containsKey(file)) {
      // already read/marked file: true/false
      return null;
    }

    Boolean isPotentialYamlKeySet = file.getUserData(GITLAB_CI_YAML_POTENTIAL_KEY);
    if (isPotentialYamlKeySet != null) {
      return isPotentialYamlKeySet
              ? fileEditor -> getEditorNotificationPanel(project, file, projectService)
              : null;
    }

    final var isPotentialGitlabCIYaml = isPotentialGitlabCIYamlFile(file);
    file.putUserData(GITLAB_CI_YAML_POTENTIAL_KEY, isPotentialGitlabCIYaml);
    return isPotentialGitlabCIYaml
            ? fileEditor -> getEditorNotificationPanel(project, file, projectService)
            : null;
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

  private boolean isPotentialGitlabCIYamlFile(@NotNull VirtualFile file) {
    if (!isYamlFile(file)) {
      return false;
    }

    var document = FileDocumentManager.getInstance().getCachedDocument(file);
    String text;
    if (document != null) {
      text = document.getText();
    } else {
      try {
        text = new String(file.contentsToByteArray(), file.getCharset());
      } catch (IOException e) {
        LOG.warn("Unable to read YAML file for editor notification: " + file.getPath(), e);
        return false;
      }
    }

    var matcher = YAML_KEY_PATTERN.matcher(text);
    boolean hasGitlabCiElement = false;
    while (matcher.find()) {
      var key = matcher.group(1);
      if ("kind".equals(key)) {
        return false;
      }
      if (POTENTIAL_GITLAB_CI_ELEMENTS.contains(key)) {
        hasGitlabCiElement = true;
      }
    }
    return hasGitlabCiElement;
  }
}
