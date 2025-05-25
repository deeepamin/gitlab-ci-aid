package com.github.deeepamin.ciaid.cache;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;

import java.io.File;

public class CIAidCacheUtils {
  public static void refreshAndReadFile(Project project, File file) {
    ApplicationManager.getApplication().invokeLater(() -> {
      var virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
      if (virtualFile != null) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
          GitlabCIYamlUtils.markAsCIYamlFile(virtualFile);
          var projectService = CIAidProjectService.getInstance(project);
          projectService.readGitlabCIYamlData(project, virtualFile, false);
        });
      }
    });
  }

  public static File getCiAidCacheDir() {
     return getOrCreateDir(PathManager.getSystemPath(), CIAidCacheService.CI_AID_CACHE_DIR_NAME);
  }

  public static File getProjectsCacheDir() {
    return getOrCreateDir(getCiAidCacheDir().getPath(), "projects");
  }

  public static File getTemplatesCacheDir() {
    return getOrCreateDir(getCiAidCacheDir().getPath(), "templates");
  }

  public static File getComponentsCacheDir() {
    return getOrCreateDir(getCiAidCacheDir().getPath(), "components");
  }

  public static File getRemoteCacheDir() {
    return getOrCreateDir(getCiAidCacheDir().getPath(), "remote");
  }

  private static File getOrCreateDir(String parent, String dir) {
    File directory = new File(parent, dir);
    if (!directory.exists()) {
      //noinspection ResultOfMethodCallIgnored
      directory.mkdirs();
    }
    return directory;
  }
}
