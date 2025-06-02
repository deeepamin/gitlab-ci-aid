package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider;
import com.github.deeepamin.ciaid.references.providers.VariablesReferenceProvider;
import com.intellij.testFramework.LightVirtualFile;

import java.util.List;

public class GitlabCIYamlUtilsTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/UtilsTest");

  public void testValidGitlabCIYamlFiles() {
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
    assertNotNull(rootDir);
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    assertNotNull(gitlabCIYaml);
    assertTrue(GitlabCIYamlUtils.isValidGitlabCIYamlFile(gitlabCIYaml));

    var pipelineYml = getCIPipelineYamlFile(rootDir);
    assertNotNull(pipelineYml);
    GitlabCIYamlUtils.markAsCIYamlFile(pipelineYml);
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
    var virtualFile = GitlabCIYamlUtils.getGitlabCIYamlFile(psiYaml);
    assertTrue(virtualFile.isPresent());
    var expectedPath = getOsAgnosticPath("/src/UtilsTest/.gitlab-ci.yml");
    assertEquals(expectedPath, virtualFile.get().getPath());

    var pipelineYml = getCIPipelineYamlFile(rootDir);
    var pipelinePsiYml = getPsiManager().findFile(pipelineYml);
    assertNotNull(pipelinePsiYml);
    GitlabCIYamlUtils.markAsCIYamlFile(pipelineYml);

    assertTrue(GitlabCIYamlUtils.hasGitlabYamlFile(pipelinePsiYml));
    var pipelineYmlVirtualFile = GitlabCIYamlUtils.getGitlabCIYamlFile(pipelinePsiYml);
    assertTrue(pipelineYmlVirtualFile.isPresent());
    var expectedPipelinePath = getOsAgnosticPath("/src/UtilsTest/pipeline.yml");
    assertEquals(expectedPipelinePath, pipelineYmlVirtualFile.get().getPath());
  }

  public void testHasGitlabYamlFiles() {
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
    var nonGitlabYamlFile = getYamlFile(rootDir, "other.yml");
    var psiYaml = getPsiManager().findFile(nonGitlabYamlFile);
    assertFalse(GitlabCIYamlUtils.hasGitlabYamlFile(psiYaml));
    GitlabCIYamlUtils.markAsUserCIYamlFile(nonGitlabYamlFile, getProject());
    assertTrue(GitlabCIYamlUtils.hasGitlabYamlFile(psiYaml));
  }

  public void testGetInputs() {
    assertEquals("inputs.test", GitlabCIYamlUtils.getInputs("$[[inputs.test]]").getFirst().path());
    assertEquals("inputs.test", GitlabCIYamlUtils.getInputs("$[[ inputs.test ]]").getFirst().path());
    assertEquals("inputs.test", GitlabCIYamlUtils.getInputs("$[[inputs.test ]]").getFirst().path());
    assertEquals("inputs.test", GitlabCIYamlUtils.getInputs("$[[ inputs.test]]").getFirst().path());
    assertEquals("inputs.test", GitlabCIYamlUtils.getInputs("   $[[ inputs.test ]]  ").getFirst().path());

    assertEmpty(GitlabCIYamlUtils.getInputs("$[[ input.test ]]"));
    assertEmpty(GitlabCIYamlUtils.getInputs("[[inputs.test ]]"));
    assertEmpty(GitlabCIYamlUtils.getInputs("$[ inputs.test ]]"));
    assertEmpty(GitlabCIYamlUtils.getInputs("$[[ inputs.test ]"));
    assertEmpty(GitlabCIYamlUtils.getInputs("$$[ output.test ]]"));
  }

  public void testGetInputNameString() {
    assertEquals("test", InputsReferenceProvider.getInputNames("$[[inputs.test]]").getFirst().path());
    assertEquals("test", InputsReferenceProvider.getInputNames("$[[ inputs.test ]]").getFirst().path());
    assertEquals("test", InputsReferenceProvider.getInputNames("$[[inputs.test ]]").getFirst().path());
    assertEquals("test", InputsReferenceProvider.getInputNames("$[[ inputs.test]]").getFirst().path());
    assertEquals("test", InputsReferenceProvider.getInputNames("   $[[ inputs.test ]]  ").getFirst().path());

    assertEmpty(InputsReferenceProvider.getInputNames("$[[ input.test ]]"));
    assertEmpty(InputsReferenceProvider.getInputNames("[[inputs.test ]]"));
    assertEmpty(InputsReferenceProvider.getInputNames("$[ inputs.test ]]"));
    assertEmpty(InputsReferenceProvider.getInputNames("$$[ output.test ]]"));
  }

  public void testGetVariables() {
    assertEquals("test", VariablesReferenceProvider.getVariables("$test").getFirst().path());
    assertEquals("test", VariablesReferenceProvider.getVariables("${test}").getFirst().path());
    assertTrue(List.of("test", "test2").containsAll(VariablesReferenceProvider.getVariables("$test ${test2}").stream().map(FileUtils.StringWithStartEndRange::path).toList()));
  }
}
