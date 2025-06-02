package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.references.resolvers.JobStageReferenceResolver;
import com.github.deeepamin.ciaid.services.CIAidProjectService;

import java.io.File;

public class JobStageReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/ReferenceResolverTest/StageToStages");

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testAnotherFile() {
    var testDir = getTestDirectoryName();
    var pipelineYamlPsi = myFixture.configureByFile(TEST_DIR_PATH + File.separator + testDir + PIPELINE_YML_PATH);
    CIAidProjectService.markAsCIYamlFile(pipelineYamlPsi.getVirtualFile());
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + PIPELINE_YML_PATH, TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    assertTrue(reference instanceof JobStageReferenceResolver);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertEquals("validate", resolve.getText());
  }

  public void testSameFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    assertTrue(reference instanceof JobStageReferenceResolver);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertEquals("build", resolve.getText());
  }
}
