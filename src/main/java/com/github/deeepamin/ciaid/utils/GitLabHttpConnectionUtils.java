package com.github.deeepamin.ciaid.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class GitLabHttpConnectionUtils {
  private static final Logger LOG = Logger.getInstance(GitLabHttpConnectionUtils.class);
  private static final String GITLAB_PRIVATE_TOKEN_HEADER = "PRIVATE-TOKEN";

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
    }
    return null;
  }

  public static String resolveComponentVersion(Project project, String projectPath, String versionRef, String accessToken) throws IOException, InterruptedException {
    if (versionRef.matches("^[a-fA-F0-9]{40}$")) {
      // Direct SHA reference — no need to resolve
      return versionRef;
    }
    String encodedProject = URLEncoder.encode(projectPath, StandardCharsets.UTF_8);
    var gitlabApiUrl = CIAidSettingsState.getInstance(project).getGitLabApiUrl();

    String url = String.format(GitLabUtils.GITLAB_PROJECT_TAGS_PATH, gitlabApiUrl, encodedProject);
    try (var httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()) {
      if (accessToken == null || accessToken.isBlank()) {
        LOG.debug("Access token is required to resolve component version");
        return null;
      }
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(url))
              .header("PRIVATE-TOKEN", accessToken)
              .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != HTTP_OK) {
        LOG.debug("Couldn't resolve component version " + response.body());
        return null;
      }

      var mapper = new ObjectMapper();
      ArrayNode tags = (ArrayNode) mapper.readTree(response.body());
      List<String> versions = new ArrayList<>();
      for (JsonNode tag : tags) {
        String name = tag.get("name").asText();
        if (!name.matches(("v?\\d+(\\.\\d+){0,2}(-[a-zA-Z0-9]+)?"))) {
          continue;
        }
        if (!name.matches("^\\d.*")) {
          name = name.substring(1);
        }
        versions.add(name);
      }

      List<String> matching;
      if (versionRef.equals("~latest")) {
        matching = versions.stream()
                .filter(v -> !v.contains("-")) // skip pre-releases
                .sorted(Comparator.reverseOrder())
                .toList();
      } else {
        matching = versions.stream()
                .filter(v -> v.startsWith(versionRef + "."))
                .sorted(Comparator.reverseOrder())
                .toList();
      }

      return matching.isEmpty() ? null : matching.getFirst();
    }
  }

  private static String getUserAgent() {
    var applicationInfo = ApplicationInfo.getInstance();
    return applicationInfo.getBuild().getProductCode() + "/" + applicationInfo.getFullVersion();
  }
}
