package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.intellij.openapi.project.Project;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GitLabUtils {
  private static final String GITLAB_PROJECT_FILES_RAW_DOWNLOAD_PATH = "%s/projects/%s/repository/files/%s/raw";

  public static String getProjectFileDownloadUrl(Project project, String projectName, String file, String ref) {
    var gitlabApiUrl = CIAidSettingsState.getInstance(project).getGitLabApiUrl();
    if (projectName.startsWith("/")) {
      projectName = projectName.substring(1);
    }
    String encodedProject = URLEncoder.encode(projectName, StandardCharsets.UTF_8);
    if (file.startsWith("/")) {
      file = file.substring(1);
    }
    String encodedFile = URLEncoder.encode(file, StandardCharsets.UTF_8);
    var downloadUrl = String.format(GITLAB_PROJECT_FILES_RAW_DOWNLOAD_PATH, gitlabApiUrl, encodedProject, encodedFile);
    var refPlaceholder = "?ref=%s";
    if (ref != null && !ref.isBlank()) {
      ref = String.format(refPlaceholder, ref);
      downloadUrl = downloadUrl.concat(ref);
    }
    return downloadUrl;
  }
}
