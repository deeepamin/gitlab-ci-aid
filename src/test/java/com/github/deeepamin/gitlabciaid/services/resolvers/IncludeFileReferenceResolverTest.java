package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.BaseTest;

public class IncludeFileReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/ReferenceResolverTest/IncludeFile";

  public void testSameDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    var pipelineCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + "pipeline.yml";
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath, pipelineCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof IncludeFileReferenceResolver);
    // reference resolve is null due to project basePath not returning copied dir in tests, so skipping that
  }

  public void testSameDirectoryQuotedIncludeText() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    var pipelineCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + "pipeline.yml";
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath, pipelineCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof IncludeFileReferenceResolver);
    // reference resolve is null due to project basePath not returning copied dir in tests, so skipping that
  }


  public void testAnotherDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    var pipelineCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/ci/" + PIPELINE_YML;
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath, pipelineCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof IncludeFileReferenceResolver);
  }

}
