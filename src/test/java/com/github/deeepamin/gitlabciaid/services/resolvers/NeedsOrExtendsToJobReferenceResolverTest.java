package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.BaseTest;
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl;

public class NeedsOrExtendsToJobReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH_NEEDS = "/ReferenceResolverTest/NeedsToJob";
  private static final String TEST_DIR_PATH_EXTENDS = "/ReferenceResolverTest/ExtendsToJob";

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testNeedsJobInSameFile() {
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH_NEEDS + "/" + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals("build-dev", ((YAMLKeyValueImpl) resolve).getKeyText());
  }

  public void testNeedsJobInAnotherFile() {
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH_NEEDS + "/ci" + PIPELINE_YML, TEST_DIR_PATH_NEEDS + "/" + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals("deploy-job", ((YAMLKeyValueImpl) resolve).getKeyText());
  }

  public void testExtendsJobInSameFile() {
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH_EXTENDS + "/ci" + PIPELINE_YML);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals(".extend-job", ((YAMLKeyValueImpl) resolve).getKeyText());
  }

  public void testExtendsJobInAnotherFile() {
    var reference = myFixture.getReferenceAtCaretPosition( TEST_DIR_PATH_EXTENDS + "/" + GITLAB_CI_DEFAULT_YAML_FILE, TEST_DIR_PATH_EXTENDS + "/ci" + PIPELINE_YML);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals(".extend-another-job", ((YAMLKeyValueImpl) resolve).getKeyText());
  }
}
