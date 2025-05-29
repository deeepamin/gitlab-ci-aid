package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.intellij.openapi.project.Project;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class GitLabUtils {
  public static final String DEFAULT_GITLAB_TEMPLATE_PROJECT = "gitlab-org/gitlab";
  public static final String DEFAULT_GITLAB_TEMPLATE_PATH = "lib/gitlab/ci/templates";
  public static final String DEFAULT_GITLAB_SERVER_URL = "https://gitlab.com";
  private static final String GITLAB_PROJECT_FILES_RAW_DOWNLOAD_PATH = "%s/projects/%s/repository/files/%s/raw";
  public static final String GITLAB_PROJECT_TAGS_PATH = "%s/projects/%s/repository/tags";

  public static String getRepositoryFileDownloadUrl(Project project, String projectName, String file, String ref) {
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

  public static String getProjectFileCacheKey(String projectName, String file, String ref) {
    return projectName + "_" + file + (ref != null ? "_" + ref : "");
  }

  public static ComponentProjectNameVersion getComponentProjectNameAndVersion(String componentPath) {
    if (componentPath == null || componentPath.isBlank()) {
      return null;
    }
    String regex = "^(?:https?://[^/]+|\\$CI_SERVER_FQDN)/(.+?)/([^/@]+)@([^?]+)(?:\\?.*)?$";
    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(componentPath);
    if (matcher.find()) {
      String project = matcher.group(1);
      String component = matcher.group(2);
      String version = matcher.group(3);
      if (project == null || component == null || version == null) {
        return null;
      }
      return new ComponentProjectNameVersion(project, component, version);
    }
    return null;
  }

  public record ComponentProjectNameVersion(String project, String component, String version) {
  }
}
