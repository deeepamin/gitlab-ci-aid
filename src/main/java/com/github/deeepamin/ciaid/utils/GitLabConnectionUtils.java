package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static java.net.HttpURLConnection.HTTP_OK;

public class GitLabConnectionUtils {
  private static final Logger LOG = Logger.getInstance(GitLabConnectionUtils.class);
  public static final String DEFAULT_GITLAB_SERVER_API_URL = "https://gitlab.com/api/v4";
  public static final String GITLAB_PROJECT_TAGS_PATH = "%s/projects/%s/repository/tags";
  private static final String GITLAB_PRIVATE_TOKEN_HEADER = "PRIVATE-TOKEN";
  private static final String GITLAB_PROJECT_FILES_RAW_DOWNLOAD_PATH = "%s/projects/%s/repository/files/%s/raw";

  public static String downloadContent(final String urlString, final String accessToken) {
    try (var httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()) {
      HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
              .uri(URI.create(urlString))
              .timeout(Duration.ofSeconds(30))
              .header("User-Agent", getUserAgent())
              .header("Client-Name", "CI Aid for GitLab Plugin");

      if (accessToken != null && !accessToken.isBlank()) {
        requestBuilder.header(GITLAB_PRIVATE_TOKEN_HEADER, accessToken);
      }
      HttpRequest request = requestBuilder.build();
      HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() != HTTP_OK) {
        LOG.debug("Error downloading file from " + urlString + " " + response);
        return null;
      }
      try (final BufferedReader in = new BufferedReader(new InputStreamReader(response.body()))) {
        var res = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          res.append(inputLine).append(System.lineSeparator());
        }
        return res.toString();
      }
    } catch (IOException | InterruptedException e) {
      LOG.debug("Exception while calling URL" + urlString + " " + e);
    } catch (IllegalArgumentException e) {
      LOG.debug("Invalid URL: " + urlString + " " + e);
    }
    return null;
  }

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

  private static String getUserAgent() {
    var applicationInfo = ApplicationInfo.getInstance();
    return applicationInfo.getBuild().getProductCode() + "/" + applicationInfo.getFullVersion();
  }
}
