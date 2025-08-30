package com.github.deeepamin.ciaid.cache.providers;

import com.intellij.openapi.project.Project;

import java.nio.file.Paths;

import static com.github.deeepamin.ciaid.cache.CIAidCacheUtils.sha256;

public class RemoteUrlIncludeProvider extends AbstractRemoteIncludeProvider {

  public RemoteUrlIncludeProvider(Project project, String filePath) {
    super(project, filePath);
  }

  @Override
  protected String getCacheDirName() {
    return "remote";
  }

  @Override
  protected void readRemoteIncludeFile() {
    var fileName = sha256(filePath) + ".yml";
    var cacheFilePath = Paths.get(getCacheDir().getAbsolutePath(), fileName).toString();
    validateAndCacheRemoteFile(filePath, filePath, cacheFilePath);
  }

  @Override
  public String getProjectPath() {
    return null;
  }

  @Override
  protected String getAccessToken() {
    return null;
  }
}
