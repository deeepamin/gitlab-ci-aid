package com.github.deeepamin.ciaid.settings;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.cache.providers.AbstractRemoteIncludeProvider;
import com.github.deeepamin.ciaid.cache.providers.ProjectFileIncludeProvider;
import com.github.deeepamin.ciaid.settings.remotes.Remote;

import java.util.List;

public class CIAidSettingsStateTest extends BaseTest {

  public void testGetMatchingProjectPathAbsolute() {
    var remotes = List.of(new Remote().projectPath("group1/group2/project1").apiUrl("https://gitlab.com/api/v4"),
            new Remote().projectPath("group1/project1").apiUrl("https://gitlab.com/api/v4"));
    var matchingProjectPath = getMatchingProjectPath(remotes, "group1/group2/project1");
    assertEquals("group1/group2/project1", matchingProjectPath);
  }

  public void testGetMatchingProjectPathGroup() {
    var remotes = List.of(new Remote().projectPath("group1/group2").apiUrl("https://gitlab.com/api/v4"),
            new Remote().projectPath("group1/project1").apiUrl("https://gitlab.com/api/v4"));
    var matchingProjectPath = getMatchingProjectPath(remotes, "group1/group2/project1");
    assertEquals("group1/group2", matchingProjectPath);
  }

  public void testGetMatchingProjectPathAbsoluteWithSlashes() {
    var remotes = List.of(new Remote().projectPath("group1/group2/project1").apiUrl("https://gitlab.com/api/v4"),
            new Remote().projectPath("group1/project1").apiUrl("https://gitlab.com/api/v4"));
    var matchingProjectPath = getMatchingProjectPath(remotes, "/group1/group2/project1");
    assertEquals("group1/group2/project1", matchingProjectPath);
  }

  public void testGetMatchingProjectPathNoMatch() {
    var remotes = List.of(new Remote().projectPath("group1/group2").apiUrl("https://gitlab.com/api/v4"),
            new Remote().projectPath("group1/project1").apiUrl("https://gitlab.com/api/v4"));
    var matchingProjectPath = getMatchingProjectPath(remotes, "group1/group4/project1");
    assertNull(matchingProjectPath);
  }

  private String getMatchingProjectPath(List<Remote> remotes, String projectPath) {
    var ciAidSettingsState = CIAidSettingsState.getInstance(getProject());
    ciAidSettingsState.setRemotes(remotes);
    AbstractRemoteIncludeProvider remoteIncludeProvider = new ProjectFileIncludeProvider(getProject(), "file.yml", projectPath, null);
    return ciAidSettingsState.getMatchingProjectPath(remoteIncludeProvider.getProjectPath());
  }
}
