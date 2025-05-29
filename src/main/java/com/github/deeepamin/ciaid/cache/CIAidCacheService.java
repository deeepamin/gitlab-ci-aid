package com.github.deeepamin.ciaid.cache;

import com.github.deeepamin.ciaid.cache.model.CIAidGitLabCacheMetadata;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabHttpConnectionUtils;
import com.github.deeepamin.ciaid.utils.GitLabUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.deeepamin.ciaid.cache.CIAidCacheUtils.getTemplatesCacheDir;
import static com.github.deeepamin.ciaid.cache.CIAidCacheUtils.sha256;
import static com.github.deeepamin.ciaid.utils.GitLabUtils.getProjectFileCacheKey;

@Service(Service.Level.APP)
@State(
        name = "CIAidCacheService",
        storages = {@Storage("CIAidCacheService.xml")}
)
public final class CIAidCacheService implements PersistentStateComponent<CIAidCacheService.State> {
  private static final Logger LOG = Logger.getInstance(CIAidCacheService.class);
  public static final String CI_AID_CACHE_DIR_NAME = "gitlab-ci-aid-cache";

  public static class State {
    public Map<String, CIAidGitLabCacheMetadata> filePathToCache = new ConcurrentHashMap<>();
    public Map<String, String> remoteIncludeIdentifierToLocalPath = new ConcurrentHashMap<>();
  }
  private final State state = new State();

  public static CIAidCacheService getInstance() {
    return ApplicationManager.getApplication().getService(CIAidCacheService.class);
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull CIAidCacheService.State state) {
    XmlSerializerUtil.copyBean(state, this.state);
  }

  public void cacheProjectFile(Project project, String projectName, String filePath, String ref) {
    if (projectName == null || filePath == null || projectName.isBlank() || filePath.isBlank()) {
      LOG.debug("Project name or filePath name is null or empty");
      return;
    }
    var downloadUrl = GitLabUtils.getRepositoryFileDownloadUrl(project, projectName, filePath, ref);

    var fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
    if (!YamlUtils.hasYamlExtension(fileName)) {
      LOG.debug("File is not a YAML file: " + fileName);
      return;
    }
    var cacheKey = getProjectFileCacheKey(projectName, filePath, ref);

    // remove the file name from the path to get the directory
    filePath = filePath.substring(0, filePath.lastIndexOf("/"));
    var projectFileDirectoryString = projectName.replaceAll("/", "_") +
            File.separator +
            (ref != null && !ref.isBlank() ? ref + File.separator : "") +
            filePath.replaceAll("/", File.separator);
    var fileAbsolutePath = Paths.get(CIAidCacheUtils.getProjectsCacheDir().getAbsolutePath()).resolve(projectFileDirectoryString).resolve(fileName);
    validateAndCacheRepositoryFile(project, downloadUrl, fileAbsolutePath.toString(), cacheKey, true);
  }

  public void cacheTemplate(Project project, String template) {
    if (template == null || template.isBlank()) {
      LOG.debug("Template path is null or empty");
      return;
    }
    var templatesProject = CIAidSettingsState.getInstance(project).getGitlabTemplatesProject();
    var templatesPathInGitLabUrl = CIAidSettingsState.getInstance(project).getGitlabTemplatesPath() + "/" + template;
    var templatesPathFileSystem = getTemplatesCacheDir().toPath().resolve(CIAidSettingsState.getInstance(project).getGitlabTemplatesPath()).resolve(template).toString();
    var downloadUrl = GitLabUtils.getRepositoryFileDownloadUrl(project, templatesProject, templatesPathInGitLabUrl, null);
    validateAndCacheRepositoryFile(project, downloadUrl, templatesPathFileSystem, template, false);
  }

  public void cacheRemoteFile(Project project, String remoteUrl) {
    if (remoteUrl == null || remoteUrl.isBlank()) {
      LOG.debug("Remote URL is null or empty");
      return;
    }
    var remoteCacheDir = CIAidCacheUtils.getRemoteCacheDir();
    var fileName = sha256(remoteUrl) + ".yml";
    var filePath = Paths.get(remoteCacheDir.getAbsolutePath(), fileName).toString();
    validateAndCacheRepositoryFile(project, remoteUrl, filePath, remoteUrl, false);
  }

  public void cacheComponent(Project project, String componentPath) {
    var componentProjectNameVersion = GitLabUtils.getComponentProjectNameAndVersion(componentPath);
    if (componentProjectNameVersion == null) {
      LOG.debug("Component path does not match the expected format: " + componentPath);
      return;
    }
    var projectName = componentProjectNameVersion.project();
    var componentName = componentProjectNameVersion.component();
    var versionRef = componentProjectNameVersion.version();

    if (projectName == null || componentName == null) {
      LOG.debug("Component project name or component name is null");
      return;
    }

    String resolvedVersion = null;
    try {
      resolvedVersion = GitLabHttpConnectionUtils.resolveComponentVersion(project, projectName, versionRef,
              CIAidSettingsState.getInstance(project).getCachedGitLabAccessToken());
    } catch (IOException | InterruptedException e) {
      LOG.debug("Error resolving component version: " + e.getMessage());
    }
    var templatesPathInGitLabUrl = "templates";
    var possiblePaths = List.of(
            templatesPathInGitLabUrl + (componentName.startsWith("/") ? "" : "/") + componentName + (componentName.endsWith("/") ? "" : "/") + "template.yml",
            templatesPathInGitLabUrl + (componentName.startsWith("/") ? "" : "/") + componentName + ".yml"
    );

    String finalResolvedVersion = resolvedVersion;
    for (String path : possiblePaths) {
      var downloadUrl = GitLabUtils.getRepositoryFileDownloadUrl(project, projectName, path, finalResolvedVersion);
      var projectFilePath = projectName.replaceAll("/", "_") +
              File.separator +
              (finalResolvedVersion != null && !finalResolvedVersion.isBlank() ? finalResolvedVersion + File.separator : "") +
              path.replaceAll("/", File.separator);

      var absolutePath = Paths.get(CIAidCacheUtils.getComponentsCacheDir().getAbsolutePath()).resolve(projectFilePath);
      validateAndCacheRepositoryFile(project, downloadUrl, absolutePath.toString(), componentPath, true);
    }
  }

  private void validateAndCacheRepositoryFile(Project project, String downloadUrl, String filePath, String cacheKey, boolean authorize) {
    //TODO check sha and expiry time
    if (downloadUrl == null || filePath == null || downloadUrl.isBlank() || filePath.isBlank()) {
      LOG.debug("Download URL or file path is null or empty");
      return;
    }
    if (state.remoteIncludeIdentifierToLocalPath.containsKey(cacheKey)) {
      LOG.debug("File already cached: " + cacheKey);
      return;
    }
    cacheFile(project, downloadUrl, filePath, cacheKey, authorize);
  }

  private void cacheFile(Project project, String downloadUrl, String filePath, String cacheKey, boolean authorize) {
    new Task.Backgroundable(project, "Resolving GitLab CI includes", false) {
      @Override
      public void run(@NotNull final ProgressIndicator indicator) {
        File file = new File(filePath);
        if (file.exists()) {
          //noinspection ResultOfMethodCallIgnored
          file.delete(); //TODO
        }
        var gitlabAccessToken = authorize ? CIAidSettingsState.getInstance(project).getGitLabAccessToken() : null;
        var content = GitLabHttpConnectionUtils.downloadContent(downloadUrl, gitlabAccessToken);
        if (content == null) {
          return;
        }
        File parent = file.getParentFile();
        if (!parent.exists()) {
          //noinspection ResultOfMethodCallIgnored
          parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
          writer.write(content);
          var expiryTime = CIAidSettingsState.getInstance(project).getCacheExpiryTime();
          var metadata = new CIAidGitLabCacheMetadata()
                  .path(filePath)
                  .expiryTime(expiryTime * 60 * 60)
                  .sha("");  //TODO get sha of file
          updateCache(cacheKey, filePath, metadata);
          CIAidCacheUtils.refreshAndReadFile(project, file);
        } catch (IOException e) {
          LOG.error("Error while caching file " + downloadUrl + ": " + e);
        }
      }
    }.queue();
  }


  public void loadCacheFromDisk(@NotNull Project project) {
    //TODO check sha and expiry time
    state.filePathToCache.forEach((path, metadata) -> {
      File file = new File(path);
      if (file.exists()) {
        CIAidCacheUtils.refreshAndReadFile(project, file);
      } else {
        LOG.warn("Cached file does not exist: " + path);
      }
    });
  }

  public void updateCache(String cacheKey, String path, CIAidGitLabCacheMetadata metadata) {
    state.filePathToCache.put(path, metadata);
    addIncludeIdentifierToLocalPath(cacheKey, path);
  }

  public void addIncludeIdentifierToLocalPath(String cacheKey, String localPath) {
    if (cacheKey == null || localPath == null) {
      LOG.debug("Cache key or local path is null");
      return;
    }
    state.remoteIncludeIdentifierToLocalPath.put(cacheKey, localPath);
  }

  public String getIncludePathFromCacheKey(String cacheKey) {
    if (cacheKey == null) {
      return null;
    }
    return state.remoteIncludeIdentifierToLocalPath.get(cacheKey);
  }
}
