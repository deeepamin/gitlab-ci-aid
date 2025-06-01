package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.cache.CIAidCacheUtils;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.settings.remotes.Remote;
import com.github.deeepamin.ciaid.utils.GitLabHttpConnectionUtils;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;

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

  protected abstract String getProjectPath();

  protected File getCacheDir() {
    return getOrCreateDir(getCiAidCacheDir().getPath(), getCacheDirName());
  }

  protected String getAccessTokenForMatchingProject(String projectPath) {
    var matchingProjectPath = getMatchingProjectPath(projectPath);
    if (matchingProjectPath == null) {
      LOG.debug("No matching project path found for " + projectPath);
      return null;
    }
    return CIAidSettingsState.getInstance(project).getGitLabAccessToken(matchingProjectPath);
  }

  public String getMatchingProjectPath(String projectPath) {
    // separate public method for testing purposes
    if (projectPath == null) {
      return null;
    }
    var sanitizedProjectPath = projectPath.startsWith("/") ? projectPath.substring(1) : projectPath;
    var ciAidSettings = CIAidSettingsState.getInstance(project);
    var settingsPaths = ciAidSettings.getRemotes().stream()
            .map(Remote::getProjectPath)
            .toList();
    if (settingsPaths.contains(sanitizedProjectPath)) {
      return sanitizedProjectPath;
    }
    return settingsPaths.stream()
            .filter(sanitizedProjectPath::startsWith)
            .max(Comparator.comparingInt(String::length)) // Get the longest matching path
            .orElse(null);
  }

  protected void validateAndCacheRemoteFile(String downloadUrl, String cacheKey, String cacheFilePath) {
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
        var content = GitLabHttpConnectionUtils.downloadContent(downloadUrl, accessToken);
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
