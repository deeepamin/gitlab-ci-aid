package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.BaseTest;

public class StagesToJobStageReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/ReferenceResolverTest/StagesToStage";

  public void testSameFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    assertTrue(reference instanceof StagesToJobStageReferenceResolver);
    var resolve = ((StagesToJobStageReferenceResolver) reference).multiResolve(true);
    assertNotNull(resolve);
  }

  public void testAnotherFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE, TEST_DIR_PATH + "/" + testDir + "/ci" + PIPELINE_YML);
    assertNotNull(reference);
    assertTrue(reference instanceof StagesToJobStageReferenceResolver);
  }
}
