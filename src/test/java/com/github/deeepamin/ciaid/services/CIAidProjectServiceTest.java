package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.BaseTest;
import com.intellij.testFramework.LightVirtualFile;

public class CIAidProjectServiceTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/UtilsTest");

  public void testValidGitlabCIYamlFiles() {
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
    assertNotNull(rootDir);
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
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    var psiYaml = getPsiManager().findFile(gitlabCIYaml);
    assertNotNull(psiYaml);
    assertTrue(CIAidProjectService.hasGitlabYamlFile(psiYaml));
    var virtualFile = CIAidProjectService.getGitlabCIYamlFile(psiYaml);
    assertTrue(virtualFile.isPresent());
    var expectedPath = getOsAgnosticPath("/src/UtilsTest/.gitlab-ci.yml");
    assertEquals(expectedPath, virtualFile.get().getPath());

    var pipelineYml = getCIPipelineYamlFile(rootDir);
    var pipelinePsiYml = getPsiManager().findFile(pipelineYml);
    assertNotNull(pipelinePsiYml);
    CIAidProjectService.markAsCIYamlFile(pipelineYml);

    assertTrue(CIAidProjectService.hasGitlabYamlFile(pipelinePsiYml));
    var pipelineYmlVirtualFile = CIAidProjectService.getGitlabCIYamlFile(pipelinePsiYml);
    assertTrue(pipelineYmlVirtualFile.isPresent());
    var expectedPipelinePath = getOsAgnosticPath("/src/UtilsTest/pipeline.yml");
    assertEquals(expectedPipelinePath, pipelineYmlVirtualFile.get().getPath());
  }

  public void testHasGitlabYamlFiles() {
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
    var nonGitlabYamlFile = getYamlFile(rootDir, "other.yml");
    var psiYaml = getPsiManager().findFile(nonGitlabYamlFile);
    assertFalse(CIAidProjectService.hasGitlabYamlFile(psiYaml));
    CIAidProjectService.markAsUserCIYamlFile(nonGitlabYamlFile, getProject());
    assertTrue(CIAidProjectService.hasGitlabYamlFile(psiYaml));
  }
}
