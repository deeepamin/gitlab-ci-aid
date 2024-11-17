package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.BaseTest;

public class ScriptReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/ReferenceResolverTest/Script";

  public void testSameDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof ScriptReferenceResolver);
    // reference resolve is null due to project basePath not returning copied dir in tests, so skipping that
  }

  public void testAnotherDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof ScriptReferenceResolver);
  }

}
