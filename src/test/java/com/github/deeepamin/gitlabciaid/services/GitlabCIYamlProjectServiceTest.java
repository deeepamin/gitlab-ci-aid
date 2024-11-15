package com.github.deeepamin.gitlabciaid.services;

import com.github.deeepamin.gitlabciaid.BaseTest;
import java.util.List;

public class GitlabCIYamlProjectServiceTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/UtilsTest";
  private static boolean dataRead = false;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if (!dataRead) {
      var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, "../");
      GitlabCIYamlProjectService.clearPluginData();
      readCIYamls(rootDir);
      dataRead = true;
    }
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testReadGitlabCIYamlData() {
    var pluginData = GitlabCIYamlProjectService.getPluginData();
    assertNotNull(pluginData);
    assertEquals(2, pluginData.size());
    var gitlabCIYamlPath = "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    var expectedFiles = List.of(gitlabCIYamlPath, PIPELINE_YML);
    assertTrue(pluginData.keySet().containsAll(expectedFiles));
    var yamlData = pluginData.get(gitlabCIYamlPath);
    assertNotNull(yamlData);
    assertEquals(1, yamlData.getIncludedYamls().size());
    assertEquals(4, yamlData.getJobs().size());
    assertEquals(3, yamlData.getStages().size());
    assertNotNull(yamlData.getStagesElement());

    var pipelineCIYamlData = pluginData.get(PIPELINE_YML);
    assertNotNull(pipelineCIYamlData);
    assertEquals(0, pipelineCIYamlData.getIncludedYamls().size());
    assertEquals(1, pipelineCIYamlData.getJobs().size());
    assertEquals(1, pipelineCIYamlData.getStages().size());
    assertNull(pipelineCIYamlData.getStagesElement());
  }

  public void testGetJobNames() {
    var jobNames = GitlabCIYamlProjectService.getJobNames();
    var expectedJobNames = List.of("build-dev", "build-sit", "test-job", "deploy-job", "checkstyle");
    assertTrue(jobNames.containsAll(expectedJobNames));
  }

  public void testGetStageNamesDefinedAtStagesLevel() {
    var stages = GitlabCIYamlProjectService.getStageNamesDefinedAtStagesLevel();
    var expectedStages = List.of("validate", "build", "test", "deploy");
    assertTrue(stages.containsAll(expectedStages));
  }

  public void testGetStageNamesDefinedAtJobLevel() {
    var stages = GitlabCIYamlProjectService.getStageNamesDefinedAtJobLevel();
    var expectedStages = List.of("validate", "build", "test", "deploy");
    assertTrue(stages.containsAll(expectedStages));
  }

  public void testGetFileName() {
    var job = "checkstyle";
    var jobFileName = GitlabCIYamlProjectService.getFileName(getProject(), (entry) -> entry.getValue().getJobs().containsKey(job));
    assertEquals(PIPELINE_YML, jobFileName);
    var stage = "build";
    var stageFileName = GitlabCIYamlProjectService.getFileName(getProject(), (entry) -> entry.getValue().getStages().containsKey(stage));
    var gitlabCIYamlPath = "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    assertEquals(gitlabCIYamlPath, stageFileName);
  }
}
