package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;

import java.io.File;

public class JobStageToStagesReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/ReferenceResolverTest/StageToStages");

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  // TODO re enable
  public void _testAnotherFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + PIPELINE_YML, TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    assertTrue(reference instanceof JobStageToStagesReferenceResolver);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertEquals("validate", resolve.getText());
  }

  public void testSameFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    assertTrue(reference instanceof JobStageToStagesReferenceResolver);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertEquals("build", resolve.getText());
  }
}
