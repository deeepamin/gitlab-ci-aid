package com.github.deeepamin.ciaid.cache;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.openapi.application.ApplicationManager;
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

  public static void refreshAndReadFile(Project project, File file, boolean dropPsiCache) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      var virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
      if (virtualFile != null) {
        CIAidProjectService.markAsCIYamlFile(virtualFile);
        var projectService = CIAidProjectService.getInstance(project);
        projectService.readGitlabCIYamlData(virtualFile, false, false);
      }
      if (dropPsiCache) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
          if (!project.isDisposed()) {
            PsiManager.getInstance(project).dropPsiCaches();
          }
        } else {
          ApplicationManager.getApplication().invokeLater(() -> {
            if (!project.isDisposed()) {
              PsiManager.getInstance(project).dropPsiCaches();
            }
          });
        }
      }
    });
  }

  public static File getOrCreateDir(String parent, String dir) {
    File directory = new File(parent, dir);
    if (!directory.exists()) {
      //noinspection ResultOfMethodCallIgnored
      directory.mkdirs();
    }
    return directory;
  }

  public static String getProjectFileCacheKey(String projectName, String file, String ref) {
    return projectName + "_" + file + (ref != null ? "_" + ref : "");
  }
}
