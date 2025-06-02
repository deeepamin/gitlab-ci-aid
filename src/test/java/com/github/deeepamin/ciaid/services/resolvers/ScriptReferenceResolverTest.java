package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.references.resolvers.ScriptReferenceResolver;

import java.io.File;

public class ScriptReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/ReferenceResolverTest/Script");

  public void testSameDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE;
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof ScriptReferenceResolver);
    // reference resolve is null due to project basePath not returning copied dir in tests, so skipping that
  }

  public void testAnotherDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE;
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof ScriptReferenceResolver);
  }

}
