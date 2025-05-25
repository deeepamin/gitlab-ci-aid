package com.github.deeepamin.ciaid.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Service(Service.Level.PROJECT)
@State(
        name = "CIAidSettingsState",
        storages = {@Storage("CIAidSettingsState.xml")}
)
public final class CIAidSettingsState implements PersistentStateComponent<CIAidSettingsState> {
  public String defaultGitlabCIYamlPath = "";
  public boolean ignoreUndefinedJob;
  public boolean ignoreUndefinedStage;
  public boolean ignoreUndefinedScript;
  public boolean ignoreUndefinedInclude;
  public Map<String, Boolean> yamlToUserMarkings = new HashMap<>();
  public Long cacheExpiryTime = 24L; // Default to 24 hours

  // GitLab specific settings
  public String gitlabServerUrl = "";
  public String gitlabComponentsPath = "";

  public static CIAidSettingsState getInstance(Project project) {
    return project.getService(CIAidSettingsState.class);
  }

  public void addYamlToUserMarking(VirtualFile file, boolean ignore) {
    yamlToUserMarkings.putIfAbsent(file.getPath(), ignore);
  }

  @Override
  public void initializeComponent() {
    getGitLabAccessToken();
  }

  @Override
  public CIAidSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull CIAidSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public String getGitLabServerUrl() {
    if (gitlabServerUrl.isEmpty()) {
      return "https://gitlab.com";
    }
    return gitlabServerUrl;
  }

  public String getGitLabApiUrl() {
    return getGitLabServerUrl() + "/api/v4";
  }

  public String getGitLabComponentsPath() {
    if (gitlabComponentsPath.isEmpty()) {
      return "lib/gitlab/ci/templates";
    }
    return gitlabComponentsPath;
  }

  public String getGitLabAccessToken() {
    var credentialAttributes = getCredentialAttributes(gitlabServerUrl);
    return PasswordSafe.getInstance().getPassword(credentialAttributes);
  }

  public void saveGitLabAccessToken(String gitlabServerUrl, String token) {
    PasswordSafe.getInstance().setPassword(getCredentialAttributes(gitlabServerUrl), token);
  }

  private CredentialAttributes getCredentialAttributes(String gitlabServerUrl) {
    return new CredentialAttributes("CIAidGitlabAccessToken", gitlabServerUrl);
  }
}
