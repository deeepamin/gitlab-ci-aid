package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.intellij.openapi.project.Project;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GitLabUtils {
  public static final String DEFAULT_GITLAB_SERVER_API_URL = "https://gitlab.com/api/v4";
  private static final String GITLAB_PROJECT_FILES_RAW_DOWNLOAD_PATH = "%s/projects/%s/repository/files/%s/raw";
  public static final String GITLAB_PROJECT_TAGS_PATH = "%s/projects/%s/repository/tags";

  public static String getRepositoryFileDownloadUrl(Project project, String projectName, String file, String ref) {
    var gitlabApiUrl = CIAidSettingsState.getInstance(project).getGitLabApiUrl(projectName);
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
