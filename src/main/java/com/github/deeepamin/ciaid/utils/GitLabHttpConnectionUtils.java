package com.github.deeepamin.ciaid.utils;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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


  private static String getUserAgent() {
    var applicationInfo = ApplicationInfo.getInstance();
    return applicationInfo.getBuild().getProductCode() + "/" + applicationInfo.getFullVersion();
  }
}
