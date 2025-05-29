package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabUtils;
import com.intellij.openapi.project.Project;

public class TemplateIncludeProvider extends AbstractRemoteIncludeProvider {

  public TemplateIncludeProvider(Project project, String filePath) {
    super(project, filePath);
  }

  @Override
  protected String getCacheDirName() {
    return "templates";
  }

  @Override
  public void readRemoteIncludeFile() {
    var templatesProject = CIAidSettingsState.getInstance(project).getGitlabTemplatesProject();

    var templatesPathInGitLabUrl = CIAidSettingsState.getInstance(project).getGitlabTemplatesPath() + "/" + filePath;
    var downloadUrl = GitLabUtils.getRepositoryFileDownloadUrl(project, templatesProject, templatesPathInGitLabUrl, null);
    var cacheFilePath = getCacheDir().toPath().resolve(CIAidSettingsState.getInstance(project).getGitlabTemplatesPath()).resolve(filePath).toString();
    validateAndCacheRemoteFile(downloadUrl, templatesPathInGitLabUrl, cacheFilePath);
  }
}
