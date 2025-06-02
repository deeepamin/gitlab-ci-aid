package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.model.gitlab.inputs.Input;
import com.github.deeepamin.ciaid.parser.CIAidGitLabYamlParser;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class CIAidProjectServiceTest extends BaseTest {
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
  public void testValidGitlabCIYamlFiles() {
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    assertNotNull(gitlabCIYaml);
    assertTrue(CIAidProjectService.isValidGitlabCIYamlFile(gitlabCIYaml));

    var pipelineYml = getCIPipelineYamlFile(rootDir);
    assertNotNull(pipelineYml);
    CIAidProjectService.markAsCIYamlFile(pipelineYml);
    assertTrue(CIAidProjectService.isValidGitlabCIYamlFile(pipelineYml));
  }

  public void testGitlabCIYamlFileWhenPathIsInvalidOrNull() {
    assertFalse(CIAidProjectService.isValidGitlabCIYamlFile(new LightVirtualFile("", "")));
    assertFalse(CIAidProjectService.isValidGitlabCIYamlFile(null));
  }

  public void testValidGetGitlabCIYamlFiles() {
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    var psiYaml = getPsiManager().findFile(gitlabCIYaml);
    assertNotNull(psiYaml);
    assertTrue(CIAidProjectService.hasGitlabYamlFile(psiYaml));
    var virtualFile = CIAidProjectService.getGitlabCIYamlFile(psiYaml);
    assertTrue(virtualFile.isPresent());
    var expectedPath = getOsAgnosticPath("/.gitlab-ci.yml");
    assertEquals(expectedPath, virtualFile.get().getPath());

    var pipelineYml = getCIPipelineYamlFile(rootDir);
    var pipelinePsiYml = getPsiManager().findFile(pipelineYml);
    assertNotNull(pipelinePsiYml);
    CIAidProjectService.markAsCIYamlFile(pipelineYml);

    assertTrue(CIAidProjectService.hasGitlabYamlFile(pipelinePsiYml));
    var pipelineYmlVirtualFile = CIAidProjectService.getGitlabCIYamlFile(pipelinePsiYml);
    assertTrue(pipelineYmlVirtualFile.isPresent());
    var expectedPipelinePath = getOsAgnosticPath("/pipeline.yml");
    assertEquals(expectedPipelinePath, pipelineYmlVirtualFile.get().getPath());
  }

  public void testHasGitlabYamlFiles() {
    var nonGitlabYamlFile = getYamlFile(rootDir, "other.yml");
    var psiYaml = getPsiManager().findFile(nonGitlabYamlFile);
    assertFalse(CIAidProjectService.hasGitlabYamlFile(psiYaml));
    CIAidProjectService.markAsUserCIYamlFile(nonGitlabYamlFile, getProject());
    assertTrue(CIAidProjectService.hasGitlabYamlFile(psiYaml));
  }

  // CIAidYamlDataProviderTest tests below, as the data is read in setUp() method
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

  // CIAidGitLabYamlParser tests below
  public void testParseGitlabCIYamlDataValidFiles() {
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    var parser = new CIAidGitLabYamlParser(getProject());
    var gitlabCIYamlData = parser.parseGitlabCIYamlData(gitlabCIYaml);

    var includedYamls = gitlabCIYamlData.getIncludes();
    assertEquals(1, includedYamls.size());
    assertEquals(PIPELINE_YML_PATH, includedYamls.getFirst().getPath());
    var expectedStages = List.of("build", "test", "deploy");
    var stageNames = gitlabCIYamlData.getJobStageElements().stream()
            .map(PsiElement::getText)
            .distinct()
            .toList();
    assertEquals(3, stageNames.size());
    assertTrue(expectedStages.containsAll(stageNames));
    Function<String, List<String>> jobStageFunction = (String stageName) -> gitlabCIYamlData.getJobStageElements().stream()
            .map(PsiElement::getText)
            .filter(stage -> stage.equals(stageName))
            .toList();
    assertEquals(3, jobStageFunction.apply("build").size());
    assertEquals(1, jobStageFunction.apply("test").size());
    assertEquals(1, jobStageFunction.apply("deploy").size());

    var expectedJobNames = List.of(".extend-test","build-dev", "build-sit", "test-job", "deploy-job");
    var jobNames = gitlabCIYamlData.getJobElements().stream()
            .map(YAMLKeyValue::getKeyText)
            .toList();
    assertEquals(5, jobNames.size());
    assertTrue(expectedJobNames.containsAll(jobNames));
    assertNotNull(gitlabCIYamlData.getFile());
    assertNotNull(gitlabCIYamlData.getStagesItemElements());
  }
}
