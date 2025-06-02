package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.references.resolvers.StagesReferenceResolver;

import java.io.File;

public class StagesReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/ReferenceResolverTest/StagesToStage");

  public void testSameFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    assertTrue(reference instanceof StagesReferenceResolver);
    var resolve = ((StagesReferenceResolver) reference).multiResolve(true);
    assertNotNull(resolve);
  }

  public void testAnotherFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE, TEST_DIR_PATH + File.separator + testDir + File.separator + "ci" + PIPELINE_YML_PATH);
    assertNotNull(reference);
    assertTrue(reference instanceof StagesReferenceResolver);
  }
}
