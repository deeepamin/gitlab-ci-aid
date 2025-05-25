package com.github.deeepamin.ciaid.cache;

import com.github.deeepamin.ciaid.cache.model.CIAidGitLabCacheMetadata;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabHttpConnectionUtils;
import com.github.deeepamin.ciaid.utils.GitLabUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.APP)
@State(
        name = "CIAidCacheService",
        storages = {@Storage("CIAidCacheService.xml")}
)
public final class CIAidCacheService implements PersistentStateComponent<CIAidCacheService.State> {
  public static final String CI_AID_CACHE_DIR_NAME = "gitlab-ci-aid-cache";
  private static final Logger LOG = Logger.getInstance(CIAidCacheService.class);
  public static final Key<String> DOWNLOAD_URL_KEY = Key.create("CIAid.Gitlab.Download.URL");

  public static class State {
    public Map<String, CIAidGitLabCacheMetadata> pathToCache = new ConcurrentHashMap<>();
    public Map<String, String> downloadUrlToLocalPath = new ConcurrentHashMap<>();
  }
  private final State state = new State();

  public static CIAidCacheService getInstance() {
    return ApplicationManager.getApplication().getService(CIAidCacheService.class);
  }

  @Override
  public CIAidCacheService.State getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull CIAidCacheService.State state) {
    XmlSerializerUtil.copyBean(state, this.state);
  }

  public void cacheProjectFile(Project project, String projectName, String file, String ref) {
    var downloadUrl = GitLabUtils.getProjectFileDownloadUrl(project, projectName, file, ref);
    var projectFilePath = projectName.replaceAll("/", "_") +
            File.separator +
            (ref != null && !ref.isBlank() ? ref + File.separator : "") +
            file.replaceAll("/", File.separator);
    Path absolutePath = null;
    try {
      absolutePath = Paths.get(CIAidCacheUtils.getProjectsCacheDir().getAbsolutePath()).resolve(projectFilePath);
      Files.createDirectories(absolutePath);
    } catch (IOException e) {
      LOG.debug("Error creating cache directory for project files: " + e.getMessage());
    }
    cacheFile(project, downloadUrl, absolutePath.toString());
  }

  private void cacheFile(Project project, String downloadUrl, String filePath) {
    //TODO check sha and expiry time

    new Task.Backgroundable(project, "Resolving GitLab CI includes", false) {
      @Override
      public void run(@NotNull final ProgressIndicator indicator) {
        File file = new File(filePath);
        if (file.exists()) {
          //noinspection ResultOfMethodCallIgnored
          file.delete(); //TODO
        }

        var gitlabAccessToken = CIAidSettingsState.getInstance(project).getGitLabAccessToken();
        var content = GitLabHttpConnectionUtils.downloadContent(downloadUrl, gitlabAccessToken);
        if (content == null) {
          return;
        }
        try (FileWriter writer = new FileWriter(file)) {
          writer.write(content);
          var expiryTime = CIAidSettingsState.getInstance(project).cacheExpiryTime;
          var filePath = file.getPath();
          var metadata = new CIAidGitLabCacheMetadata()
                  .path(filePath)
                  .expiryTime(expiryTime * 60 * 60)
                  .sha("");  //TODO get sha of file
          updateCache(filePath, downloadUrl, metadata);
          CIAidCacheUtils.refreshAndReadFile(project, file);
        } catch (IOException e) {
          LOG.error("Error while caching file " + downloadUrl + ": " + e);
        }
      }
    }.queue();
  }


  public void loadCacheFromDisk(@NotNull Project project) {
    //TODO check sha and expiry time
    state.pathToCache.forEach((path, metadata) -> {
      File file = new File(path);
      if (file.exists()) {
        CIAidCacheUtils.refreshAndReadFile(project, file);
      } else {
        LOG.warn("Cached file does not exist: " + path);
      }
    });
  }

  public void updateCache(String path, String downloadUrl, CIAidGitLabCacheMetadata metadata) {
    state.pathToCache.put(path, metadata);
    state.downloadUrlToLocalPath.put(downloadUrl, path);
  }

  public String getIncludePathFromDownloadUrl(String downloadUrl) {
    return state.downloadUrlToLocalPath.get(downloadUrl);
  }
}
