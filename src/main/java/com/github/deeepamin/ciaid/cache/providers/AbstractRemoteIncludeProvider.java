package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.cache.CIAidCacheUtils;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabHttpConnectionUtils;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.github.deeepamin.ciaid.cache.CIAidCacheService.getCiAidCacheDir;
import static com.github.deeepamin.ciaid.cache.CIAidCacheUtils.getOrCreateDir;

public abstract class AbstractRemoteIncludeProvider extends AbstractIncludeProvider {
  protected AbstractRemoteIncludeProvider(Project project, String filePath) {
    super(project, filePath);
  }

  @Override
  public void readIncludeFile() {
    if (filePath == null) {
      LOG.debug("File path is null for " + this.getClass().getSimpleName());
      return;
    }
    readRemoteIncludeFile();
  }

  protected abstract void readRemoteIncludeFile();

  protected String getGitLabAccessToken() {
    if (this instanceof RemoteUrlIncludeProvider) {
      // Remote URL includes do not support authorization
      return null;
    }
    return CIAidSettingsState.getInstance(project).getGitLabAccessToken();
  }

  protected File getCacheDir() {
    return getOrCreateDir(getCiAidCacheDir().getPath(), getCacheDirName());
  }

  protected abstract String getCacheDirName();

  protected void validateAndCacheRemoteFile(String downloadUrl, String cacheKey, String cacheFilePath) {
    //TODO check sha and expiry time
    var includePath = CIAidCacheService.getInstance().getIncludeCacheFilePathFromKey(cacheKey);
    if (includePath != null) {
      LOG.debug("File already cached " + cacheFilePath + " for key " + cacheKey);
      return;
    }
    if (downloadUrl == null || downloadUrl.isBlank()) {
      LOG.debug("Download URL is null or empty for " + this.getClass().getSimpleName());
      return;
    }
    cacheRemoteFile(downloadUrl, cacheKey, cacheFilePath);
  }

  private void cacheRemoteFile(String downloadUrl, String cacheKey, String cacheFilePath) {
    new Task.Backgroundable(project, "Resolving GitLab CI includes", false) {
      @Override
      public void run(@NotNull final ProgressIndicator indicator) {
        File file = new File(cacheFilePath);
        if (file.exists()) {
          //noinspection ResultOfMethodCallIgnored
          file.delete(); //TODO
        }
        var content = GitLabHttpConnectionUtils.downloadContent(downloadUrl, getGitLabAccessToken());
        if (content == null) {
          LOG.debug("Failed to download content from " + downloadUrl);
          return;
        }
        File parent = file.getParentFile();
        if (!parent.exists()) {
          //noinspection ResultOfMethodCallIgnored
          parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
          writer.write(content);
          var sha = ""; //TODO get sha of file
          updateCache(cacheKey, cacheFilePath, sha);
          CIAidCacheUtils.refreshAndReadFile(project, file);
        } catch (IOException e) {
          LOG.error("Error while caching file " + downloadUrl + ": " + e);
        }
      }
    }.queue();
  }

  private void updateCache(String cacheKey, String cacheFilePath, String sha) {
    var ciAidCacheService = CIAidCacheService.getInstance();
    ciAidCacheService.addFilePathToStateCache(project, cacheFilePath, sha);
    ciAidCacheService.addIncludeIdentifierToCacheFilePath(cacheKey, cacheFilePath);
  }

}
