package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabConnectionUtils;
import com.intellij.openapi.project.Project;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class TemplateIncludeProvider extends AbstractRemoteIncludeProvider {
  private static final String DEFAULT_GITLAB_TEMPLATE_PROJECT = "gitlab-org/gitlab";
  private static final String DEFAULT_GITLAB_TEMPLATE_PATH = "lib/gitlab/ci/templates";

  public TemplateIncludeProvider(Project project, String filePath) {
    super(project, filePath);
  }

  @Override
  protected String getCacheDirName() {
    return "templates";
  }

  @Override
  public String getProjectPath() {
    var templatesProject = CIAidSettingsState.getInstance(project).getGitlabTemplatesProject();
    if (templatesProject == null) {
      templatesProject = DEFAULT_GITLAB_TEMPLATE_PROJECT;
    }
    return templatesProject;
  }

  @Override
  public void readRemoteIncludeFile() {
    var templatesProject = getProjectPath();
    var templatesPath = CIAidSettingsState.getInstance(project).getGitlabTemplatesPath();
    if (templatesPath == null) {
      templatesPath = DEFAULT_GITLAB_TEMPLATE_PATH;
    }

    var templatesPathInGitLabUrl = templatesPath + "/" + filePath;
    var downloadUrl = GitLabConnectionUtils.getRepositoryFileDownloadUrl(project, templatesProject, templatesPathInGitLabUrl, null);
    Path cacheRoot = getCacheDir().toPath().toAbsolutePath().normalize();
    Path templatePath;
    Path includePath;
    try {
      templatePath = Path.of(templatesPath);
      includePath = Path.of(filePath);
    } catch (InvalidPathException e) {
      LOG.warn("Invalid GitLab template path: " + e.getInput());
      return;
    }
    if (templatePath.isAbsolute() || templatePath.getRoot() != null
            || includePath.isAbsolute() || includePath.getRoot() != null) {
      LOG.warn("Skipping GitLab template with an absolute path");
      return;
    }
    Path cacheFile = cacheRoot.resolve(templatePath).resolve(includePath).normalize();
    if (!cacheFile.startsWith(cacheRoot)) {
      LOG.warn("Skipping GitLab template path outside the cache directory");
      return;
    }
    var cacheFilePath = cacheFile.toString();
    validateAndCacheRemoteFile(downloadUrl, templatesPathInGitLabUrl, cacheFilePath);
  }
}
