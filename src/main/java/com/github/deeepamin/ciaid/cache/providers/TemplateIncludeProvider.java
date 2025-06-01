package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabUtils;
import com.intellij.openapi.project.Project;

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
  protected String getProjectPath() {
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
    var downloadUrl = GitLabUtils.getRepositoryFileDownloadUrl(project, templatesProject, templatesPathInGitLabUrl, null);
    var cacheFilePath = getCacheDir().toPath().resolve(templatesPath).resolve(filePath).toString();
    validateAndCacheRemoteFile(downloadUrl, templatesPathInGitLabUrl, cacheFilePath);
  }
}
