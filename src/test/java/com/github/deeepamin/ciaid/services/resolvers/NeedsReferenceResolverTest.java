package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl;

import java.io.File;

public class NeedsReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH_NEEDS = getOsAgnosticPath("/ReferenceResolverTest/NeedsToJob");
  private static final String TEST_DIR_PATH_EXTENDS = getOsAgnosticPath("/ReferenceResolverTest/ExtendsToJob");

  @Override
  public void setUp() throws Exception {
    super.setUp();
    var needToJobDirPipelineYml = myFixture.configureByFile(TEST_DIR_PATH_NEEDS + File.separator + "ci" + PIPELINE_YML_PATH);
    var extendsToJobDirPipelineYml = myFixture.configureByFile(TEST_DIR_PATH_EXTENDS + File.separator + "ci" + PIPELINE_YML_PATH);
    CIAidProjectService.markAsCIYamlFile(needToJobDirPipelineYml.getVirtualFile());
    CIAidProjectService.markAsCIYamlFile(extendsToJobDirPipelineYml.getVirtualFile());
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testNeedsJobInSameFile() {
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH_NEEDS + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals("build-dev", ((YAMLKeyValueImpl) resolve).getKeyText());
  }

  public void testNeedsJobInAnotherFile() {
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH_NEEDS + File.separator + "ci" + PIPELINE_YML_PATH, TEST_DIR_PATH_NEEDS + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals("deploy-job", ((YAMLKeyValueImpl) resolve).getKeyText());
  }

  public void testExtendsJobInSameFile() {
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH_EXTENDS + File.separator + "ci" + PIPELINE_YML_PATH);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals(".extend-job", ((YAMLKeyValueImpl) resolve).getKeyText());
  }

  public void testExtendsJobInAnotherFile() {
    var reference = myFixture.getReferenceAtCaretPosition( TEST_DIR_PATH_EXTENDS + File.separator + GITLAB_CI_DEFAULT_YAML_FILE, TEST_DIR_PATH_EXTENDS +  File.separator + "ci" + PIPELINE_YML_PATH);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValueImpl);
    assertEquals(".extend-another-job", ((YAMLKeyValueImpl) resolve).getKeyText());
  }
}
