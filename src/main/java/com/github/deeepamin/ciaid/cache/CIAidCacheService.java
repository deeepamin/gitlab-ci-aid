package com.github.deeepamin.ciaid.cache;

import com.github.deeepamin.ciaid.cache.model.CIAidGitLabCacheMetadata;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.APP)
@State(
        name = "CIAidCacheService",
        storages = {@Storage("CIAidCacheService.xml")}
)
public final class CIAidCacheService implements PersistentStateComponent<CIAidCacheService.State> {
  private static final Logger LOG = Logger.getInstance(CIAidCacheService.class);
  public static final String CI_AID_CACHE_DIR_NAME = "gitlab-ci-aid-cache";

  public static class State {

    public Map<String, CIAidGitLabCacheMetadata> filePathToCache = new ConcurrentHashMap<>();
    public Map<String, String> remoteIncludeIdentifierToLocalPath = new ConcurrentHashMap<>();
  }
  private final State state = new State();
  public static CIAidCacheService getInstance() {
    return ApplicationManager.getApplication().getService(CIAidCacheService.class);
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull CIAidCacheService.State state) {
    XmlSerializerUtil.copyBean(state, this.state);
  }

  public static File getCiAidCacheDir() {
    return CIAidCacheUtils.getOrCreateDir(PathManager.getSystemPath(), CI_AID_CACHE_DIR_NAME);
  }

  public void loadCacheFromDisk(@NotNull Project project) {
    state.filePathToCache.forEach((path, metadata) -> {
      File file = new File(path);
      if (file.exists()) {
        CIAidCacheUtils.refreshAndReadFile(project, file);
      } else {
        LOG.warn("Cached file does not exist: " + path);
      }
    });
  }

  public void addIncludeIdentifierToCacheFilePath(String cacheKey, String cacheFilePath) {
    if (cacheKey == null || cacheFilePath == null) {
      LOG.debug("Cache key or local path is null");
      return;
    }
    state.remoteIncludeIdentifierToLocalPath.put(cacheKey, cacheFilePath);
  }

  public String getIncludeCacheFilePathFromKey(String cacheKey) {
    if (cacheKey == null) {
      return null;
    }
    return state.remoteIncludeIdentifierToLocalPath.get(cacheKey);
  }

  public void addFilePathToStateCache(Project project, String filePath, String sha) {
    if (filePath == null) {
      LOG.debug("File path is null");
      return;
    }
    var metadata = new CIAidGitLabCacheMetadata()
            .path(filePath)
            .expiryTime(CIAidSettingsState.getInstance(project).getCacheExpiryTime() * 60 * 60)
            .sha(sha);
    state.filePathToCache.put(filePath, metadata);
  }
}
