package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.model.gitlab.inputs.Input;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.io.File;
import java.util.List;

public class CIAidYamlDataProviderTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/UtilsTest");
  private static boolean dataRead = false;
  protected static VirtualFile rootDir;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if (!dataRead) {
      rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, "../");
      var projectService = getProject().getService(CIAidProjectService.class);
      projectService.getDataProvider().clearPluginData();
      readCIYamls(rootDir);
      dataRead = true;
    }
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
    assertEquals(1, yamlData.getIncludes().size());
    assertEquals(5, yamlData.getJobElements().size());
    var distinctJobStageNames = yamlData.getJobStageElements().stream()
            .map(PsiElement::getText)
            .distinct()
            .toList();
    assertEquals(3, distinctJobStageNames.size());
    assertEquals(0, yamlData.getInputs().size());
    assertNotNull(yamlData.getStagesItemElements());

    var pipelineCIYamlData = pluginData.get(pipelineCIYamlFile);
    assertNotNull(pipelineCIYamlData);
    assertEquals(0, pipelineCIYamlData.getIncludes().size());
    assertEquals(2, pipelineCIYamlData.getJobElements().size());
    assertEquals(1, pipelineCIYamlData.getJobStageElements().size());
    assertEquals(4, pipelineCIYamlData.getInputs().size());
    assertTrue(pipelineCIYamlData.getStagesItemElements().isEmpty());
  }

  public void testGetJobNames() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var jobNames = projectService.getDataProvider().getJobNames();
    var expectedJobNames = List.of("build-dev", ".extend-test", "build-sit", "test-job", "deploy-job", "checkstyle");
    assertTrue(jobNames.containsAll(expectedJobNames));
  }

  public void testGetStageNamesDefinedAtStagesLevel() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var stages = projectService.getDataProvider().getStageNamesDefinedAtStagesLevel();
    var expectedStages = List.of("validate", "build", "test", "deploy");
    assertTrue(stages.containsAll(expectedStages));
  }

  public void testGetStageNamesDefinedAtJobLevel() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var stages = projectService.getDataProvider().getStageNamesDefinedAtJobLevel();
    var expectedStages = List.of("validate", "build", "test", "deploy");
    assertTrue(stages.containsAll(expectedStages));
  }

  public void testGetFileName() {
    var job = "checkstyle";
    var projectService = getProject().getService(CIAidProjectService.class);
    var jobFileName = projectService.getDataProvider().getFileName((entry) -> entry.getValue().getJobElements()
            .stream()
            .anyMatch(jobKeyValue -> jobKeyValue.getKeyText().equals(job)));
    assertTrue(jobFileName.contains(PIPELINE_YML_PATH));
    var stage = "build";
    var stageFileName = projectService.getDataProvider().getFileName((entry) -> entry.getValue().getJobStageElements()
            .stream()
            .anyMatch(jobStage -> jobStage.getText().equals(stage)));
    var gitlabCIYamlPath = File.separator + GITLAB_CI_DEFAULT_YAML_FILE;
    assertTrue(stageFileName.contains(gitlabCIYamlPath));
  }

  public void testGetJobFileName() {
    var job = "checkstyle";
    var fileName = getProject().getService(CIAidProjectService.class).getDataProvider().getJobFileName(job);
    assertTrue(fileName.contains(PIPELINE_YML_PATH));
  }

  public void testGetJobStageFileName() {
    var stage = "build";
    var fileName = getProject().getService(CIAidProjectService.class).getDataProvider().getJobStageFileName(stage);
    assertTrue(fileName.contains(GITLAB_CI_DEFAULT_YAML_FILE));
  }

  public void testGetStagsItemFileName() {
    var stagesItem = "deploy";
    var fileName = getProject().getService(CIAidProjectService.class).getDataProvider().getStagesItemFileName(stagesItem);
    assertTrue(fileName.contains(GITLAB_CI_DEFAULT_YAML_FILE));
  }

  public void testGetInputs() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var inputNames = projectService.getDataProvider().getInputs()
            .stream()
            .map(Input::name)
            .toList();
    var expectedInputNames = List.of("name", "stage", "context", "tag");
    assertTrue(inputNames.containsAll(expectedInputNames));
  }

  public void testGetVariables() {
    var projectService = getProject().getService(CIAidProjectService.class);
    var variablesAndContainingFiles = projectService.getDataProvider().getVariableAndContainingFiles(projectService);
    assertNotNull(variablesAndContainingFiles);
    assertTrue(variablesAndContainingFiles.containsKey("TEST_VAR1"));
    assertTrue(variablesAndContainingFiles.containsKey("DOCKER_TAG"));
    var filesContainingEnvVariable = variablesAndContainingFiles.get("TEST_VAR1");
    assertNotNull(filesContainingEnvVariable);
    assertEquals(2, filesContainingEnvVariable.size());
  }
}
