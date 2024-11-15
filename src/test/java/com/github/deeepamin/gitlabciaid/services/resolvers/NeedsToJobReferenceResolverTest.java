package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.BaseTest;
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl;

public class NeedsToJobReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/ReferenceResolverTest/NeedsToJob";

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testNeedsJobInSameFile() {
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + "/" + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals("build-dev", ((YAMLKeyValueImpl) resolve).getKeyText());
  }

  public void testNeedsJobInAnotherFile() {
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + "/ci" + PIPELINE_YML, TEST_DIR_PATH + "/" + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals("deploy-job", ((YAMLKeyValueImpl) resolve).getKeyText());
  }
}
