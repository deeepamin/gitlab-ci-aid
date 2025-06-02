package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.model.CIAidYamlData;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.Map;

import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.GITLAB_CI_DEFAULT_YAML_FILES;

@Service(Service.Level.PROJECT)
public final class CIAidProjectService implements DumbAware, Disposable {
  private static final Logger LOG = Logger.getInstance(CIAidProjectService.class);
  private final Project project;
  private final CIAidYamlDataProvider dataProvider;

  public CIAidProjectService(Project project) {
    this.project = project;
    this.dataProvider = new CIAidYamlDataProvider(project);
  }

  public static CIAidProjectService getInstance(Project project) {
    return project.getService(CIAidProjectService.class);
  }

  public static void executeOnThreadPool(final Runnable runnable) {
    ApplicationManager.getApplication().executeOnPooledThread(runnable);
  }

  public CIAidYamlDataProvider getDataProvider() {
    return dataProvider;
  }

  public Map<VirtualFile, CIAidYamlData> getPluginData() {
    return getDataProvider().getPluginData();
  }

  public void afterStartup() {
    readDefaultGitlabCIYaml();
    readUserMarkedYamls();
  }

  public void processOpenedFile(VirtualFile file) {
    if (!GitlabCIYamlUtils.isValidGitlabCIYamlFile(file)) {
      return;
    }
    readGitlabCIYamlData(file, GitlabCIYamlUtils.isMarkedAsUserCIYamlFile(file), false);
  }

  public void readGitlabCIYamlData(VirtualFile file, boolean userMarked, boolean forceRead) {
    getDataProvider().readGitlabCIYamlData(file, userMarked, forceRead);
  }

  private void readDefaultGitlabCIYaml() {
    final var ciAidSettingsState = CIAidSettingsState.getInstance(project);
    if (!ciAidSettingsState.getDefaultGitlabCIYamlPath().isBlank()) {
      final var defaultGitlabCIYamlPath = ciAidSettingsState.getDefaultGitlabCIYamlPath();
      final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(defaultGitlabCIYamlPath);
      if (gitlabCIYamlFile != null) {
        readGitlabCIYamlData(gitlabCIYamlFile, false, false);
        return;
      }
    }
    // if default yaml path is not set, check for default gitlab ci yaml files
    final var basePath = project.getBasePath();
    for (var yamlFile : GITLAB_CI_DEFAULT_YAML_FILES) {
      final var gitlabCIYamlPath = basePath + File.separator + yamlFile;
      final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(gitlabCIYamlPath);
      if (gitlabCIYamlFile != null) {
        LOG.info("Found " + yamlFile + " in " + gitlabCIYamlPath);
        readGitlabCIYamlData(gitlabCIYamlFile, false, false);
        break;
      }
    }
  }

  private void readUserMarkedYamls() {
    final var ciAidSettingsState = CIAidSettingsState.getInstance(project);
    ciAidSettingsState.getYamlToUserMarkings().forEach((path, ignore) -> {
      final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (gitlabCIYamlFile != null) {
        if (ignore) {
          GitlabCIYamlUtils.ignoreCIYamlFile(gitlabCIYamlFile, project);
          return;
        }
        readGitlabCIYamlData(gitlabCIYamlFile, true, false);
      }
    });
  }

  @Override
  public void dispose() {
    getDataProvider().clearPluginData();
  }
}
