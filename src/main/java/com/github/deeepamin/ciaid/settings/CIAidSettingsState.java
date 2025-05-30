package com.github.deeepamin.ciaid.settings;

import com.github.deeepamin.ciaid.utils.GitLabUtils;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Service(Service.Level.PROJECT)
@State(
        name = "CIAidSettingsState",
        storages = {@Storage("CIAidSettingsState.xml")}
)
public final class CIAidSettingsState implements PersistentStateComponent<CIAidSettingsState.State> {
  public static class State {
    public String defaultGitlabCIYamlPath = "";
    public boolean ignoreUndefinedJob;
    public boolean ignoreUndefinedStage;
    public boolean ignoreUndefinedScript;
    public boolean ignoreUndefinedInclude;
    public Map<String, Boolean> yamlToUserMarkings = new HashMap<>();
    public Long cacheExpiryTime = 24L; // Default to 24 hours

    // GitLab specific settings
    public String gitlabServerUrl = "";
    public String gitlabTemplatesProject = "";
    public String gitlabTemplatesPath = "";
  }

  private State state = new State();
  private final Project project;
  private String cachedGitLabAccessToken;

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
  public void initializeComponent() {
    getGitLabAccessToken();
  }

  @Override
  public CIAidSettingsState.State getState() {
    return this.state;
  }

  @Override
  public void loadState(@NotNull CIAidSettingsState.State state) {
    this.cachedGitLabAccessToken = getGitLabAccessToken();
    this.state = state;
  }

  public String getGitLabServerUrl() {
    if (getGitlabServerUrl().isEmpty()) {
      return GitLabUtils.DEFAULT_GITLAB_SERVER_URL;
    }
    return getGitlabServerUrl();
  }

  public String getGitLabApiUrl() {
    return getGitLabServerUrl() + "/api/v4";
  }

  public String getGitlabTemplatesProject() {
    if (state.gitlabTemplatesProject.isBlank()) {
      return GitLabUtils.DEFAULT_GITLAB_TEMPLATE_PROJECT;
    }
    return state.gitlabTemplatesProject;
  }


  public String getGitlabTemplatesPath() {
    if (state.gitlabTemplatesPath.isBlank()) {
      return GitLabUtils.DEFAULT_GITLAB_TEMPLATE_PATH;
    }
    return state.gitlabTemplatesPath;
  }

  public String getCachedGitLabAccessToken() {
    if (cachedGitLabAccessToken == null) {
      cachedGitLabAccessToken = getGitLabAccessToken();
    }
    return cachedGitLabAccessToken;
  }

  public String getGitLabAccessToken() {
    var credentialAttributes = getCredentialAttributes(project, getGitlabServerUrl());
    return PasswordSafe.getInstance().getPassword(credentialAttributes);
  }

  public void saveGitLabAccessToken(String gitlabServerUrl, String token) {
    PasswordSafe.getInstance().setPassword(getCredentialAttributes(project, gitlabServerUrl), token);
    this.cachedGitLabAccessToken = token;
  }

  private CredentialAttributes getCredentialAttributes(Project project, String gitlabServerUrl) {
    var projectLocationHash = project.getLocationHash();
    return new CredentialAttributes("CIAidGitlabAccessToken-" + projectLocationHash, gitlabServerUrl);
  }

  public String getDefaultGitlabCIYamlPath() {
    return state.defaultGitlabCIYamlPath;
  }

  public void setDefaultGitlabCIYamlPath(String defaultGitlabCIYamlPath) {
    this.state.defaultGitlabCIYamlPath = defaultGitlabCIYamlPath;
  }

  public boolean isIgnoreUndefinedJob() {
    return state.ignoreUndefinedJob;
  }

  public void setIgnoreUndefinedJob(boolean ignoreUndefinedJob) {
    this.state.ignoreUndefinedJob = ignoreUndefinedJob;
  }

  public boolean isIgnoreUndefinedStage() {
    return state.ignoreUndefinedStage;
  }

  public void setIgnoreUndefinedStage(boolean ignoreUndefinedStage) {
    this.state.ignoreUndefinedStage = ignoreUndefinedStage;
  }

  public boolean isIgnoreUndefinedScript() {
    return state.ignoreUndefinedScript;
  }

  public void setIgnoreUndefinedScript(boolean ignoreUndefinedScript) {
    this.state.ignoreUndefinedScript = ignoreUndefinedScript;
  }

  public boolean isIgnoreUndefinedInclude() {
    return state.ignoreUndefinedInclude;
  }

  public void setIgnoreUndefinedInclude(boolean ignoreUndefinedInclude) {
    this.state.ignoreUndefinedInclude = ignoreUndefinedInclude;
  }

  public Map<String, Boolean> getYamlToUserMarkings() {
    return state.yamlToUserMarkings;
  }

  public void setYamlToUserMarkings(Map<String, Boolean> yamlToUserMarkings) {
    this.state.yamlToUserMarkings = yamlToUserMarkings;
  }

  public Long getCacheExpiryTime() {
    var expiryTimeHours = state.cacheExpiryTime;
    if (expiryTimeHours == null || expiryTimeHours <= 0) {
      // Default to 24 hours if not set or invalid
      expiryTimeHours = 24L;
      state.cacheExpiryTime = expiryTimeHours;
    }
    return expiryTimeHours * 60 * 60 * 1000; // Convert hours to milliseconds
  }

  public void setCacheExpiryTime(Long cacheExpiryTime) {
    this.state.cacheExpiryTime = cacheExpiryTime;
  }

  public String getGitlabServerUrl() {
    return state.gitlabServerUrl;
  }

  public void setGitlabServerUrl(String gitlabServerUrl) {
    this.state.gitlabServerUrl = gitlabServerUrl;
  }

  public void setGitlabTemplatesProject(String gitlabTemplatesProject) {
    this.state.gitlabTemplatesProject = gitlabTemplatesProject;
  }

  public void setGitlabTemplatesPath(String gitlabTemplatesPath) {
    this.state.gitlabTemplatesPath = gitlabTemplatesPath;
  }
}
