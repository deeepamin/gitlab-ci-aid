package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.cache.CIAidCacheUtils;
import com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabConnectionUtils;
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
    var isCachingEnabled = CIAidSettingsState.getInstance(project).isCachingEnabled();
    if (!isCachingEnabled) {
      LOG.debug("Caching is disabled for " + this.getClass().getSimpleName());
      return;
    }
    readRemoteIncludeFile();
  }

  protected abstract void readRemoteIncludeFile();

  protected abstract String getCacheDirName();

  public abstract String getProjectPath();

  protected File getCacheDir() {
    return getOrCreateDir(getCiAidCacheDir().getPath(), getCacheDirName());
  }

  protected String getAccessTokenForMatchingProject(String projectPath) {
    var ciAidSettingsState = CIAidSettingsState.getInstance(project);
    var matchingProjectPath = ciAidSettingsState.getMatchingProjectPath(projectPath);
    if (matchingProjectPath == null) {
      LOG.debug("No matching project path found for " + projectPath);
      return null;
    }
    return ciAidSettingsState.getGitLabAccessToken(matchingProjectPath);
  }

  protected void validateAndCacheRemoteFile(String downloadUrl, String cacheKey, String cacheFilePath) {
    var hasInputs = InputsReferenceProvider.isAnInputsString(downloadUrl);
    if (hasInputs) {
      LOG.debug("Contains inputs in download URL, skipping caching: " + downloadUrl);
      return;
    }

    var isCacheExpired = CIAidCacheService.getInstance().isCacheExpired(cacheFilePath);
    var includePath = CIAidCacheService.getInstance().getIncludeCacheFilePathFromKey(cacheKey);
    if (includePath != null && !isCacheExpired) {
      LOG.debug("File already cached " + cacheFilePath + " for key " + cacheKey);
      return;
    }
    if (downloadUrl == null || downloadUrl.isBlank()) {
      LOG.debug("Download URL is null or empty for " + this.getClass().getSimpleName());
      return;
    }
    var accessToken = getAccessTokenForMatchingProject(getProjectPath());
    cacheRemoteFile(downloadUrl, cacheKey, cacheFilePath, accessToken);
  }

  private void cacheRemoteFile(String downloadUrl, String cacheKey, String cacheFilePath, String accessToken) {
    new Task.Backgroundable(project, "Resolving GitLab CI includes", false) {
      @Override
      public void run(@NotNull final ProgressIndicator indicator) {
        File cacheFile = new File(cacheFilePath);
        var content = GitLabConnectionUtils.downloadContent(downloadUrl, accessToken);
        if (content == null) {
          LOG.debug("Failed to download content from " + downloadUrl);
          return;
        }
        File parent = cacheFile.getParentFile();
        if (!parent.exists()) {
          //noinspection ResultOfMethodCallIgnored
          parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(cacheFile)) {
          // overwrites the existing file or creates a new one
          writer.write(content);
          // cache file path in cache service
          updateCache(project, cacheKey, cacheFile);
        } catch (IOException e) {
          LOG.error("Error while caching file " + downloadUrl + ": " + e);
        }
      }
    }.queue();
  }

  private void updateCache(Project project, String cacheKey, File cacheFile) {
    var ciAidCacheService = CIAidCacheService.getInstance();
    ciAidCacheService.addIncludeIdentifierToCacheFilePath(cacheKey, cacheFile.getAbsolutePath());
    ciAidCacheService.addFilePathToStateCache(project, cacheFile.getAbsolutePath());
    CIAidCacheUtils.refreshAndReadFile(project, cacheFile);
  }
}
