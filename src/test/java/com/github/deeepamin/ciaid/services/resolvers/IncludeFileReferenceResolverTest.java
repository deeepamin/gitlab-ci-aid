package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.references.resolvers.LocalIncludeFileResolver;
import com.github.deeepamin.ciaid.references.resolvers.WildcardPatternIncludeFileResolver;

import java.io.File;

public class IncludeFileReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/ReferenceResolverTest/IncludeFile";

  public void testSameDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE;
    var pipelineCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + "pipeline.yml";
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath, pipelineCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof LocalIncludeFileResolver);
    // reference resolve is null due to project basePath not returning copied dir in tests, so skipping that
  }

  public void testSameDirectoryQuotedIncludeText() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE;
    var pipelineCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + "pipeline.yml";
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath, pipelineCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof LocalIncludeFileResolver);
    // reference resolve is null due to project basePath not returning copied dir in tests, so skipping that
  }

  public void testAnotherDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE;
    var pipelineCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + "ci" + File.separator + PIPELINE_YML_PATH;
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath, pipelineCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof LocalIncludeFileResolver);
  }

  public void testWildcardInclude() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE;
    var reference = myFixture.getReferenceAtCaretPosition(gitlabCIYamlPath);
    assertNotNull(reference);
    assertTrue(reference instanceof WildcardPatternIncludeFileResolver);
  }
}
