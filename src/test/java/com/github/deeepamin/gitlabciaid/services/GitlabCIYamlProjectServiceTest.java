package com.github.deeepamin.gitlabciaid.services;

import com.github.deeepamin.gitlabciaid.BaseTest;
import com.github.deeepamin.gitlabciaid.model.GitlabCIYamlData;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public class GitlabCIYamlProjectServiceTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/UtilsTest";
  private static boolean dataRead = false;
  private static VirtualFile rootDir;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if (!dataRead) {
      rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, "../");
      var projectService = getProject().getService(GitlabCIYamlProjectService.class);
      projectService.clearPluginData();
      readCIYamls(rootDir);
      dataRead = true;
    }
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testReadGitlabCIYamlData() {
    var projectService = getProject().getService(GitlabCIYamlProjectService.class);
    var pluginData = projectService.getPluginData();
    assertNotNull(pluginData);
    assertEquals(2, pluginData.size());

    var gitlabCIYamlFile = getGitlabCIYamlFile(rootDir);
    var pipelineCIYamlFile = getCIPipelineYamlFile(rootDir);
    assertTrue(pluginData.keySet().containsAll(List.of(gitlabCIYamlFile, pipelineCIYamlFile)));
    var yamlData = pluginData.get(gitlabCIYamlFile);

    assertNotNull(yamlData);
    assertEquals(1, yamlData.getIncludedYamls().size());
    assertEquals(4, yamlData.getJobs().size());
    assertEquals(3, yamlData.getStages().size());
    assertNotNull(yamlData.getStagesElement());

    var pipelineCIYamlData = pluginData.get(pipelineCIYamlFile);
    assertNotNull(pipelineCIYamlData);
    assertEquals(0, pipelineCIYamlData.getIncludedYamls().size());
    assertEquals(1, pipelineCIYamlData.getJobs().size());
    assertEquals(1, pipelineCIYamlData.getStages().size());
    assertNull(pipelineCIYamlData.getStagesElement());
  }

  public void testParseGitlabCIYamlDataValidFiles() {
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    var gitlabCIYamlData = new GitlabCIYamlData(gitlabCIYaml, gitlabCIYaml.getModificationStamp());
    var projectService = getProject().getService(GitlabCIYamlProjectService.class);
    projectService.parseGitlabCIYamlData(getProject(), gitlabCIYaml, gitlabCIYamlData);

    var includedYamls = gitlabCIYamlData.getIncludedYamls();
    assertEquals(1, includedYamls.size());
    assertEquals(PIPELINE_YML, includedYamls.getFirst());
    var expectedStages = List.of("build", "test", "deploy");
    var stageNames = gitlabCIYamlData.getStages().keySet().stream().toList();
    assertEquals(3, stageNames.size());
    assertTrue(expectedStages.containsAll(stageNames));
    assertEquals(2, gitlabCIYamlData.getStages().get("build").size());
    assertEquals(1, gitlabCIYamlData.getStages().get("test").size());
    assertEquals(1, gitlabCIYamlData.getStages().get("deploy").size());

    var expectedJobNames = List.of("build-dev", "build-sit", "test-job", "deploy-job");
    var jobNames = gitlabCIYamlData.getJobs().keySet().stream().toList();
    assertEquals(4, jobNames.size());
    assertTrue(expectedJobNames.containsAll(jobNames));
    assertNotNull(gitlabCIYamlData.getFile());
    assertNotNull(gitlabCIYamlData.getStagesElement());
  }

  public void testGetJobNames() {
    var projectService = getProject().getService(GitlabCIYamlProjectService.class);
    var jobNames = projectService.getJobNames();
    var expectedJobNames = List.of("build-dev", "build-sit", "test-job", "deploy-job", "checkstyle");
    assertTrue(jobNames.containsAll(expectedJobNames));
  }

  public void testGetStageNamesDefinedAtStagesLevel() {
    var projectService = getProject().getService(GitlabCIYamlProjectService.class);
    var stages = projectService.getStageNamesDefinedAtStagesLevel();
    var expectedStages = List.of("validate", "build", "test", "deploy");
    assertTrue(stages.containsAll(expectedStages));
  }

  public void testGetStageNamesDefinedAtJobLevel() {
    var projectService = getProject().getService(GitlabCIYamlProjectService.class);
    var stages = projectService.getStageNamesDefinedAtJobLevel();
    var expectedStages = List.of("validate", "build", "test", "deploy");
    assertTrue(stages.containsAll(expectedStages));
  }

  public void testGetFileName() {
    var job = "checkstyle";
    var projectService = getProject().getService(GitlabCIYamlProjectService.class);
    var jobFileName = projectService.getFileName(getProject(), (entry) -> entry.getValue().getJobs().containsKey(job));
    assertTrue(jobFileName.contains(PIPELINE_YML));
    var stage = "build";
    var stageFileName = projectService.getFileName(getProject(), (entry) -> entry.getValue().getStages().containsKey(stage));
    var gitlabCIYamlPath = "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    assertTrue(stageFileName.contains(gitlabCIYamlPath));
  }
}
