package com.github.deeepamin.ciaid.cache.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabConnectionUtils;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.deeepamin.ciaid.utils.GitLabConnectionUtils.GITLAB_PRIVATE_TOKEN_HEADER;
import static java.net.HttpURLConnection.HTTP_OK;

public class ComponentIncludeProvider extends AbstractRemoteIncludeProvider {
  private static final String COMPONENTS_DIR_IN_GITLAB = "templates";
  private static final String DEFAULT_COMPONENT_FILE_NAME = "template.yml";
  private String componentProjectPath;
  private String componentName;
  private String componentVersion;

  public ComponentIncludeProvider(Project project, String filePath) {
    super(project, filePath);
    var componentProjectNameVersion = getComponentProjectNameAndVersion(filePath);
    if (componentProjectNameVersion == null) {
      LOG.debug("Component path does not match the expected format: " + filePath);
      return;
    }
    componentProjectPath = componentProjectNameVersion.project();
    componentName = componentProjectNameVersion.component();
    componentVersion = componentProjectNameVersion.version();
  }

  @Override
  protected String getCacheDirName() {
    return "components";
  }

  @Override
  public String getProjectPath() {
    return componentProjectPath;
  }

  @Override
  protected void readRemoteIncludeFile() {
    if (componentProjectPath == null || componentName == null) {
      LOG.debug("Component project path or component name is null for " + this.getClass().getSimpleName());
      return;
    }
    var resolvedComponentVersion = resolveComponentVersion(project, componentVersion, getAccessToken());

    // one of the possible paths for component file in GitLab will be found
    var possiblePaths = List.of(
      COMPONENTS_DIR_IN_GITLAB + (componentName.startsWith("/") ? "" : "/") + componentName + (componentName.endsWith("/") ? "" : "/") + DEFAULT_COMPONENT_FILE_NAME,
      COMPONENTS_DIR_IN_GITLAB + (componentName.startsWith("/") ? "" : "/") + componentName + ".yml"
    );

    for (String path : possiblePaths) {
      var downloadUrl = GitLabConnectionUtils.getRepositoryFileDownloadUrl(project, componentProjectPath, path, resolvedComponentVersion);
      var projectFilePath = componentProjectPath.replaceAll("/", "_") +
              File.separator +
              (resolvedComponentVersion != null && !resolvedComponentVersion.isBlank() ? resolvedComponentVersion + File.separator : "") +
              path.replaceAll("/", File.separator);

      var cacheFilePath = Paths.get(getCacheDir().getAbsolutePath()).resolve(projectFilePath).toString();
      validateAndCacheRemoteFile(downloadUrl, filePath, cacheFilePath);
    }
  }

  public String resolveComponentVersion(Project project, String versionRef, String accessToken) {
    if (versionRef.matches("^[a-fA-F0-9]{40}$")) {
      // Direct SHA reference — no need to resolve
      return versionRef;
    }
    String encodedProject = URLEncoder.encode(componentProjectPath, StandardCharsets.UTF_8);
    var gitlabApiUrl = CIAidSettingsState.getInstance(project).getGitLabAPIUrl();

    String url = String.format(GitLabConnectionUtils.GITLAB_PROJECT_TAGS_PATH, gitlabApiUrl, encodedProject);
    try (var httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()) {

      HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
              .uri(URI.create(url));

      if (accessToken != null && !accessToken.isBlank()) {
        requestBuilder.header(GITLAB_PRIVATE_TOKEN_HEADER, accessToken);
      }

      HttpResponse<String> response;
      try {
        response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
      } catch (IOException | InterruptedException e) {
        LOG.debug("Couldn't resolve component version ");
        return versionRef;
      }

      if (response.statusCode() != HTTP_OK) {
        LOG.debug("Couldn't resolve component version " + response.body());
        return versionRef;
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
      if (versions.contains(versionRef)) {
        return versionRef; // Exact match e.g. v1.0.0 or v1.0.0-rc
      }

      var nonPreReleaseVersions = versions.stream()
              .filter(v -> !v.contains("-")) // skip pre-releases
              .sorted(Comparator.reverseOrder())
              .toList();
      if (versionRef.equals("~latest")) {
        return nonPreReleaseVersions.isEmpty() ? null : nonPreReleaseVersions.getFirst();
      } else {
        return nonPreReleaseVersions.stream()
                .filter(v -> v.startsWith(versionRef + "."))
                .max(Comparator.naturalOrder())
                .orElse(null);
      }
    } catch (JsonProcessingException e) {
      LOG.debug("Error while reading component tags " + e.getMessage());
    }
    return versionRef;
  }

  public ComponentProjectNameVersion getComponentProjectNameAndVersion(String componentPath) {
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
