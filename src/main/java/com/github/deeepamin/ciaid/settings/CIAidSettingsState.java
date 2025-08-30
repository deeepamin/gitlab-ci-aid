package com.github.deeepamin.ciaid.settings;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.credentialStore.CredentialAttributesKt.SERVICE_NAME_PREFIX;

@Service(Service.Level.PROJECT)
@State(
        name = "CIAidSettingsState",
        storages = {@Storage("CIAidSettingsState.xml")}
)
public final class CIAidSettingsState implements PersistentStateComponent<CIAidSettingsState.State> {
    private static final String DEFAULT_GITLAB_SERVER_URL = "https://gitlab.com";
    private static final String API_PATH = "api/v4";
    public static class State {
      public String defaultGitlabCIYamlPath = "";
      public boolean isEditorNotificationDisabled = false;
      public Map<String, Boolean> yamlToUserMarkings = new HashMap<>();

      public String serverUrl;
      @Transient
      public String accessToken;
      public String gitlabTemplatesProject;
      public String gitlabTemplatesPath;
      private boolean isCachingEnabled = true;
      public Long cacheExpiryTime = 24L; // Default to 24 hours
  }

  private State state = new State();
  private final Project project;

  public CIAidSettingsState(Project project) {
    this.project = project;
  }

  public static CIAidSettingsState getInstance(Project project) {
    return project.getService(CIAidSettingsState.class);
  }

  public void addYamlToUserMarking(VirtualFile file, boolean ignore) {
    getYamlToUserMarkings().putIfAbsent(file.getPath(), ignore);
  }

  @Override
  public CIAidSettingsState.State getState() {
    return this.state;
  }

  @Override
  public void loadState(@NotNull CIAidSettingsState.State state) {
    this.state = state;
  }

  @Override
  public void initializeComponent() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      this.state.accessToken = readGitLabAccessToken(state.serverUrl);
    });
  }

  public String readGitLabAccessToken(String serverUrl) {
    if (serverUrl == null) {
      return null;
    }
    var credentialAttributes = getCredentialAttributes(serverUrl);
    return PasswordSafe.getInstance().getPassword(credentialAttributes);
  }

  public void saveGitLabAccessToken(String serverUrl, String token) {
    PasswordSafe.getInstance().setPassword(getCredentialAttributes(serverUrl), token);
  }

  public String getGitLabServerUrl() {
    if (state.serverUrl != null && !state.serverUrl.isBlank()) {
      return state.serverUrl;
    }
    return DEFAULT_GITLAB_SERVER_URL;
  }

  public String getGitLabAPIUrl() {
    if (state.serverUrl != null && !state.serverUrl.isBlank()) {
      return state.serverUrl + (state.serverUrl.endsWith("/") ? "" : "/") + API_PATH;
    }
    return DEFAULT_GITLAB_SERVER_URL + "/" + API_PATH;
  }

  public String getGitLabAccessToken() {
    return state.accessToken != null ? state.accessToken : "";
  }

  public String getGitlabTemplatesProject() {
    return state.gitlabTemplatesProject;
  }

  public String getGitlabTemplatesPath() {
    return state.gitlabTemplatesPath;
  }

  private CredentialAttributes getCredentialAttributes(String gitLabServerUrl) {
    return new CredentialAttributes(SERVICE_NAME_PREFIX + " CIAidGitlabAccessToken", gitLabServerUrl);
  }

  public String getDefaultGitlabCIYamlPath() {
    return state.defaultGitlabCIYamlPath;
  }

  public void setDefaultGitlabCIYamlPath(String defaultGitlabCIYamlPath) {
    this.state.defaultGitlabCIYamlPath = defaultGitlabCIYamlPath;
  }

  public boolean isEditorNotificationDisabled() {
    return state.isEditorNotificationDisabled;
  }

  public void setEditorNotificationDisabled(boolean editorNotificationDisabled) {
    this.state.isEditorNotificationDisabled = editorNotificationDisabled;
  }

  public Map<String, Boolean> getYamlToUserMarkings() {
    return state.yamlToUserMarkings;
  }

  public void setYamlToUserMarkings(Map<String, Boolean> yamlToUserMarkings) {
    this.state.yamlToUserMarkings = yamlToUserMarkings;
  }

  public void setGitLabServerUrl(String serverUrl) {
    this.state.serverUrl = serverUrl;
  }

  public void setGitLabAccessToken(String accessToken) {
    this.state.accessToken = accessToken;
  }

  public void setGitlabTemplatesProject(String gitlabTemplatesProject) {
    this.state.gitlabTemplatesProject = gitlabTemplatesProject;
  }

  public void setGitlabTemplatesPath(String gitlabTemplatesPath) {
    this.state.gitlabTemplatesPath = gitlabTemplatesPath;
  }

  public boolean isCachingEnabled() {
    return state.isCachingEnabled;
  }

  public void setCachingEnabled(boolean cachingEnabled) {
    state.isCachingEnabled = cachingEnabled;
  }

  public Long getCacheExpiryTime() {
    var expiryTimeHours = state.cacheExpiryTime;
    if (expiryTimeHours == null || expiryTimeHours <= 0) {
      // Default to 24 hours if not set or invalid
      expiryTimeHours = 24L;
    }
    return expiryTimeHours;
  }

  public void setCacheExpiryTime(Long cacheExpiryTime) {
    this.state.cacheExpiryTime = cacheExpiryTime;
  }

  public void forceReadPluginData() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      var ciAidProjectService = CIAidProjectService.getInstance(project);
      if (ciAidProjectService != null) {
        var pluginData = ciAidProjectService.getPluginData();
        pluginData.keySet().forEach(file -> {
          var isUserMarked = CIAidProjectService.isMarkedAsUserCIYamlFile(file);
          ciAidProjectService.readGitlabCIYamlData(file, isUserMarked, true);
        });
      }
    });
  }
}
