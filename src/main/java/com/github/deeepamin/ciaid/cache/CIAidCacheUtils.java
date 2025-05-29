package com.github.deeepamin.ciaid.cache;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiManager;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CIAidCacheUtils {
  public static String sha256(String input) {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      var encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
      for (byte b : encodedHash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1)
          hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException ignored) {
    }
    return null;
  }

  public static void refreshAndReadFile(Project project, File file) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      var virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
      if (virtualFile != null) {
        GitlabCIYamlUtils.markAsCIYamlFile(virtualFile);
        var projectService = CIAidProjectService.getInstance(project);
        projectService.readGitlabCIYamlData(project, virtualFile, false);
      }
      ApplicationManager.getApplication().invokeLater(() -> PsiManager.getInstance(project).dropPsiCaches());
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
