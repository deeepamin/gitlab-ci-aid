package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;

import java.io.File;

public abstract class BaseIntegrationTest extends BaseTest {
  protected File tempCacheDir;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // Clean up the cache directory before each test to ensure isolation
    cleanupCacheDirectories();
  }

  @Override
  public void tearDown() throws Exception {
    if (tempCacheDir != null && tempCacheDir.exists()) {
      deleteDirectory(tempCacheDir);
    }
    // Clean up cache directories after each test as well
    cleanupCacheDirectories();
    super.tearDown();
  }

  protected void cleanupCacheDirectories() {
    // Clean up the actual cache directories that the providers use
    File cacheDir = CIAidCacheService.getCiAidCacheDir();
    if (cacheDir.exists()) {
      File componentsDir = new File(cacheDir, "components");
      File remoteDir = new File(cacheDir, "remote");
      File projectsDir = new File(cacheDir, "projects");

      if (componentsDir.exists()) {
        deleteDirectory(componentsDir);
      }
      if (remoteDir.exists()) {
        deleteDirectory(remoteDir);
      }
      if (projectsDir.exists()) {
        deleteDirectory(projectsDir);
      }
    }

    // Also clear the cache service state (metadata) to ensure test isolation
    var cacheService = CIAidCacheService.getInstance();
    var state = cacheService.getState();
    if (state != null) {
      state.filePathToCache.clear();
      state.remoteIncludeIdentifierToLocalPath.clear();
    }
  }

  protected void deleteDirectory(File dir) {
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          //noinspection ResultOfMethodCallIgnored
          file.delete();
        }
      }
    }
    //noinspection ResultOfMethodCallIgnored
    dir.delete();
  }

  protected void disableCaching() {
    CIAidSettingsState.getInstance(getProject()).setCachingEnabled(false);
  }
}
