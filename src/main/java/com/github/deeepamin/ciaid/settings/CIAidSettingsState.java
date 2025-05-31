package com.github.deeepamin.ciaid.settings;

import com.github.deeepamin.ciaid.settings.remotes.Remote;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.deeepamin.ciaid.utils.GitLabUtils.DEFAULT_GITLAB_SERVER_API_URL;

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

    // Remotes settings
    public List<Remote> remotes = new ArrayList<>();
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

  public String getGitLabApiUrl(String projectPath) {
    var remoteOptional = state.remotes.stream()
            .filter(remote -> remote.getProject().equals(projectPath))
            .findFirst();
    if (remoteOptional.isPresent()) {
      return remoteOptional.get().getApiUrl();
    }
    return DEFAULT_GITLAB_SERVER_API_URL;
  }

  public String getGitlabTemplatesProject() {
    return state.gitlabTemplatesProject;
  }

  public String getGitlabTemplatesPath() {
    return state.gitlabTemplatesPath;
  }

  public String getGitLabAccessToken(String projectPath) {
    var credentialAttributes = getCredentialAttributes(project, projectPath);
    return PasswordSafe.getInstance().getPassword(credentialAttributes);
  }

  public void saveGitLabAccessToken(String projectPath, String token) {
    PasswordSafe.getInstance().setPassword(getCredentialAttributes(project, projectPath), token);
  }

  private CredentialAttributes getCredentialAttributes(Project project, String projectPath) {
    var projectLocationHash = project.getLocationHash();
    return new CredentialAttributes("CIAidGitlabAccessToken-" + projectLocationHash, projectPath);
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

  public List<Remote> getRemotes() {
    return state.remotes;
  }

  public void setRemotes(List<Remote> remotes) {
    state.remotes = remotes;
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
}
