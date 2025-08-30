package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.model.CIAidYamlData;
import com.github.deeepamin.ciaid.services.listeners.CIAidPsiTreeChangeListener;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service(Service.Level.PROJECT)
public final class CIAidProjectService implements DumbAware, Disposable {
  public static final Key<Boolean> GITLAB_CI_YAML_MARKED_KEY = Key.create("CIAid.Gitlab.YAML");
  public static final Key<Boolean> GITLAB_CI_YAML_USER_MARKED_KEY = Key.create("CIAid.Gitlab.User.YAML");
  public static final String GITLAB_CI_DEFAULT_YML_FILE = ".gitlab-ci.yml";
  public static final String GITLAB_CI_DEFAULT_YAML_FILE = ".gitlab-ci.yaml";
  public static final List<String> GITLAB_CI_DEFAULT_YAML_FILES = List.of(GITLAB_CI_DEFAULT_YML_FILE, GITLAB_CI_DEFAULT_YAML_FILE);
  private static final Logger LOG = Logger.getInstance(CIAidProjectService.class);
  private final Project project;
  private final CIAidYamlDataProvider dataProvider;

  public CIAidProjectService(Project project) {
    this.project = project;
    this.dataProvider = new CIAidYamlDataProvider(project);
    PsiManager.getInstance(project)
            .addPsiTreeChangeListener(new CIAidPsiTreeChangeListener(project), DisposerService.getInstance(project));
  }

  public static CIAidProjectService getInstance(Project project) {
    return project.getService(CIAidProjectService.class);
  }

  public static CIAidProjectService getCIAidProjectService(PsiElement psiElement) {
    var service = getInstance(psiElement.getProject());
    if (service == null) {
      throw new IllegalStateException("Cannot find CI Aid project service: " + psiElement.getProject().getName());
    }
    return service;
  }

  public static void executeOnThreadPool(final Project project, final Runnable runnable) {
    DumbService.getInstance(project).runWhenSmart(() ->
            ApplicationManager.getApplication().executeOnPooledThread(runnable));
  }

  public static void markAsUserCIYamlFile(VirtualFile file, Project project) {
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, true);
    CIAidSettingsState.getInstance(project).addYamlToUserMarking(file, false);
  }

  public static void ignoreCIYamlFile(VirtualFile file, Project project) {
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, false);
    CIAidSettingsState.getInstance(project).addYamlToUserMarking(file, true);
  }

  public static void removeMarkingOfUserCIYamlFile(VirtualFile file) {
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, null);
  }

  public static boolean isMarkedAsUserCIYamlFile(VirtualFile file) {
    return Boolean.TRUE.equals(file.getUserData(GITLAB_CI_YAML_USER_MARKED_KEY));
  }

  public static void markAsCIYamlFile(VirtualFile file) {
    file.putUserData(GITLAB_CI_YAML_MARKED_KEY, true);
  }

  public static boolean isMarkedAsCIYamlFile(VirtualFile file) {
    return Boolean.TRUE.equals(file.getUserData(GITLAB_CI_YAML_MARKED_KEY));
  }

  public static boolean isValidGitlabCIYamlFile(final VirtualFile file) {
    return file != null && file.isValid() && file.exists()
            && (GITLAB_CI_DEFAULT_YAML_FILES
                      .stream()
                      .anyMatch(yamlFile -> file.getPath().endsWith(yamlFile)
            || isMarkedAsCIYamlFile(file)
            || isMarkedAsUserCIYamlFile(file)));
  }

  public static boolean hasGitlabYamlFile(final PsiElement psiElement) {
    return getGitlabCIYamlFile(psiElement).isPresent();
  }

  public static Optional<VirtualFile> getGitlabCIYamlFile(final PsiElement psiElement) {
    return Optional.ofNullable(psiElement)
            .map(PsiElement::getContainingFile)
            .map(PsiFile::getOriginalFile)
            .map(PsiFile::getViewProvider)
            .map(FileViewProvider::getVirtualFile)
            .filter(CIAidProjectService::isValidGitlabCIYamlFile);
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
    if (!isValidGitlabCIYamlFile(file)) {
      return;
    }
    readGitlabCIYamlData(file, isMarkedAsUserCIYamlFile(file), false);
  }

  public void readGitlabCIYamlData(VirtualFile file, boolean userMarked, boolean forceRead) {
    dataProvider.readGitlabCIYamlData(file, userMarked, forceRead);
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
    var entries = new ArrayList<>(ciAidSettingsState.getYamlToUserMarkings().entrySet());
    for (var entry : entries) {
      String path = entry.getKey();
      boolean ignore = entry.getValue();
      var pathContainsWildcard = CIAidUtils.containsWildcard(path);
      if (pathContainsWildcard) {
        var matchingFiles = FileUtils.findVirtualFilesByGlob(path, project);
        matchingFiles.forEach(file -> doReadUserMarkedYaml(file, ignore));
      } else {
        final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (gitlabCIYamlFile != null) {
          doReadUserMarkedYaml(gitlabCIYamlFile, ignore);
        }
      }
    }
  }

  private void doReadUserMarkedYaml(VirtualFile virtualFile, boolean ignore) {
    if (ignore) {
      ignoreCIYamlFile(virtualFile, project);
      return;
    }
    readGitlabCIYamlData(virtualFile, true, false);
  }

  @Override
  public void dispose() {
    dataProvider.clearPluginData();
  }
}
