package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;

import java.io.File;

public class InputsReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/ReferenceResolverTest/Inputs");

  public void testSameFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
  }

  public void testAnotherFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE, TEST_DIR_PATH + File.separator + testDir + PIPELINE_YML_PATH);
    assertNotNull(reference);
  }
}
