package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.model.CIAidYamlData;
import com.github.deeepamin.ciaid.model.gitlab.Input;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class CIAidProjectServiceTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/UtilsTest");
  private static boolean dataRead = false;
  private static VirtualFile rootDir;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if (!dataRead) {
      rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, "../");
      var projectService = getProject().getService(CIAidProjectService.class);
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
    var projectService = getProject().getService(CIAidProjectService.class);
    var pluginData = projectService.getPluginData();
    assertNotNull(pluginData);
    assertEquals(2, pluginData.size());

    var gitlabCIYamlFile = getGitlabCIYamlFile(rootDir);
    var pipelineCIYamlFile = getCIPipelineYamlFile(rootDir);
    assertTrue(pluginData.keySet().containsAll(List.of(gitlabCIYamlFile, pipelineCIYamlFile)));
    var yamlData = pluginData.get(gitlabCIYamlFile);

    assertNotNull(yamlData);
    assertEquals(1, yamlData.getIncludedYamls().size());
    assertEquals(5, yamlData.getJobElements().size());
    var distinctJobStageNames = yamlData.getJobStageElements().stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(pointer -> pointer.getElement().getText())
            .distinct()
            .toList();
    assertEquals(3, distinctJobStageNames.size());
    assertEquals(0, yamlData.getInputs().size());
    assertNotNull(yamlData.getStagesItemElements());

    var pipelineCIYamlData = pluginData.get(pipelineCIYamlFile);
    assertNotNull(pipelineCIYamlData);
    assertEquals(0, pipelineCIYamlData.getIncludedYamls().size());
    assertEquals(2, pipelineCIYamlData.getJobElements().size());
    assertEquals(1, pipelineCIYamlData.getJobStageElements().size());
    assertEquals(4, pipelineCIYamlData.getInputs().size());
    assertTrue(pipelineCIYamlData.getStagesItemElements().isEmpty());
  }

  public void testParseGitlabCIYamlDataValidFiles() {
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    var gitlabCIYamlData = new CIAidYamlData(gitlabCIYaml, gitlabCIYaml.getModificationStamp());
    var projectService = getProject().getService(CIAidProjectService.class);
    projectService.parseGitlabCIYamlData(getProject(), gitlabCIYaml, gitlabCIYamlData);

    var includedYamls = gitlabCIYamlData.getIncludedYamls();
    assertEquals(1, includedYamls.size());
    assertEquals(PIPELINE_YML, includedYamls.getFirst());
    var expectedStages = List.of("build", "test", "deploy");
    var stageNames = gitlabCIYamlData.getJobStageElements().stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(pointer -> pointer.getElement().getText())
            .distinct()
            .toList();
    assertEquals(3, stageNames.size());
    assertTrue(expectedStages.containsAll(stageNames));
    Function<String, List<String>> jobStageFunction = (String stageName) -> gitlabCIYamlData.getJobStageElements().stream()
                    .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
                    .map(pointer -> pointer.getElement().getText())
                    .filter(stage -> stage.equals(stageName))
                    .toList();
    assertEquals(3, jobStageFunction.apply("build").size());
    assertEquals(1, jobStageFunction.apply("test").size());
    assertEquals(1, jobStageFunction.apply("deploy").size());

    var expectedJobNames = List.of(".extend-test","build-dev", "build-sit", "test-job", "deploy-job");
    var jobNames = gitlabCIYamlData.getJobElements().stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(pointer -> pointer.getElement().getKeyText())
            .toList();
    assertEquals(5, jobNames.size());
    assertTrue(expectedJobNames.containsAll(jobNames));
    assertNotNull(gitlabCIYamlData.getFile());
    assertNotNull(gitlabCIYamlData.getStagesItemElements());
  }

  public void testGetJobNames() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var jobNames = projectService.getJobNames();
    var expectedJobNames = List.of("build-dev", ".extend-test", "build-sit", "test-job", "deploy-job", "checkstyle");
    assertTrue(jobNames.containsAll(expectedJobNames));
  }

  public void testGetStageNamesDefinedAtStagesLevel() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var stages = projectService.getStageNamesDefinedAtStagesLevel();
    var expectedStages = List.of("validate", "build", "test", "deploy");
    assertTrue(stages.containsAll(expectedStages));
  }

  public void testGetStageNamesDefinedAtJobLevel() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var stages = projectService.getStageNamesDefinedAtJobLevel();
    var expectedStages = List.of("validate", "build", "test", "deploy");
    assertTrue(stages.containsAll(expectedStages));
  }

  public void testGetFileName() {
    var job = "checkstyle";
    var projectService = getProject().getService(CIAidProjectService.class);
    var jobFileName = projectService.getFileName(getProject(), (entry) -> entry.getValue().getJobElements()
            .stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .anyMatch(pointer -> pointer.getElement().getKeyText().equals(job)));
    assertTrue(jobFileName.contains(PIPELINE_YML));
    var stage = "build";
    var stageFileName = projectService.getFileName(getProject(), (entry) -> entry.getValue().getJobStageElements()
            .stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .anyMatch(pointer -> pointer.getElement().getText().equals(stage)));
    var gitlabCIYamlPath = File.separator + GITLAB_CI_DEFAULT_YAML_FILE;
    assertTrue(stageFileName.contains(gitlabCIYamlPath));
  }

  public void testGetInputs() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var inputNames = projectService.getInputs()
            .stream()
            .map(Input::name)
            .toList();
    var expectedInputNames = List.of("name", "stage", "context", "tag");
    assertTrue(inputNames.containsAll(expectedInputNames));
  }
}
