package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.services.providers.EditorNotificationProvider;
import com.intellij.testFramework.LightVirtualFile;

import java.io.File;
import java.nio.file.Path;

public class GitlabCIYamlUtilsTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/UtilsTest";

  public void testIsGitlabCIYamlFile() {
    var ciYamlPath = Path.of(GITLAB_CI_DEFAULT_YAML_FILE);
    var result = GitlabCIYamlUtils.isGitlabCIYamlFile(ciYamlPath);
    assertTrue(result);
    var ciPipelineYamlPath = Path.of(PIPELINE_YML);
    var resultCI = GitlabCIYamlUtils.isGitlabCIYamlFile(ciPipelineYamlPath);
    assertTrue(resultCI);
  }

  public void testIsNotGitlabCIYamlFileWhenPathIsNull() {
    assertFalse(GitlabCIYamlUtils.isGitlabCIYamlFile(null));
  }

  public void testValidGitlabCIYamlFiles() {
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
    assertNotNull(rootDir);
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    assertNotNull(gitlabCIYaml);
    assertTrue(GitlabCIYamlUtils.isValidGitlabCIYamlFile(gitlabCIYaml));

    var pipelineYml = getCIPipelineYamlFile(rootDir);
    assertNotNull(pipelineYml);
    assertTrue(GitlabCIYamlUtils.isValidGitlabCIYamlFile(pipelineYml));
  }

  public void testGitlabCIYamlFileWhenPathIsInvalidOrNull() {
    assertFalse(GitlabCIYamlUtils.isValidGitlabCIYamlFile(new LightVirtualFile("", "")));
    assertFalse(GitlabCIYamlUtils.isValidGitlabCIYamlFile(null));
  }

  public void testValidGetGitlabCIYamlFiles() {
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    var psiYaml = getPsiManager().findFile(gitlabCIYaml);
    assertNotNull(psiYaml);
    assertTrue(GitlabCIYamlUtils.hasGitlabYamlFile(psiYaml));
    var path = GitlabCIYamlUtils.getGitlabCIYamlFile(psiYaml);
    assertTrue(path.isPresent());
    var expectedPath = "/src/UtilsTest/.gitlab-ci.yml".replace("/", File.separator);
    assertEquals(expectedPath, path.get().toString());

    var pipelineYml = getCIPipelineYamlFile(rootDir);
    var pipelinePsiYml = getPsiManager().findFile(pipelineYml);
    assertNotNull(pipelinePsiYml);
    assertTrue(GitlabCIYamlUtils.hasGitlabYamlFile(pipelinePsiYml));
    var pipelineYmlPath = GitlabCIYamlUtils.getGitlabCIYamlFile(pipelinePsiYml);
    assertTrue(pipelineYmlPath.isPresent());
    var expectedPipelinePath = "/src/UtilsTest/pipeline.yml".replace("/", File.separator);
    assertEquals(expectedPipelinePath, pipelineYmlPath.get().toString());
  }

  public void testHasGitlabYamlFiles() {
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
    var nonGitlabYamlFile = getYamlFile(rootDir, "other.yml");
    var psiYaml = getPsiManager().findFile(nonGitlabYamlFile);
    assertFalse(GitlabCIYamlUtils.hasGitlabYamlFile(psiYaml));
    EditorNotificationProvider.markAsGitLabYamlFile(nonGitlabYamlFile);
    assertTrue(GitlabCIYamlUtils.hasGitlabYamlFile(psiYaml));
  }
}
