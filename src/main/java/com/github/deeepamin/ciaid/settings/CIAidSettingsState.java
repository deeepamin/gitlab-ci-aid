package com.github.deeepamin.ciaid.settings;

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

  public static CIAidSettingsState getInstance(Project project) {
    return project.getService(CIAidSettingsState.class);
  }

  public void addYamlToUserMarking(VirtualFile file, boolean ignore) {
    yamlToUserMarkings.putIfAbsent(file.getPath(), ignore);
  }

  @Override
  public CIAidSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull CIAidSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
