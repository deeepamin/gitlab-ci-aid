package com.github.deeepamin.ciaid;

import com.github.deeepamin.ciaid.references.resolvers.JobStageReferenceResolver;
import com.github.deeepamin.ciaid.references.resolvers.LocalIncludeFileResolver;

public class RegressionTest extends BaseTest {
  public void testIncludeFileNameWithMultipleDotsIssue215() {
    myFixture.addFileToProject("ci/stageone.gitlab-ci.yml", """
            job1:
              stage: one
            
            job2:
              stage: two
            
            job3:
              stage: deploy
            """);
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            stages:
              - one
              - two
              - three
            
            include:
              - <caret>ci/stageone.gitlab-ci.yml
            
            job:
              stage: one
            """);

    var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    assertNotNull(reference);
    assertTrue(reference instanceof LocalIncludeFileResolver);
  }

  public void testQuotedStageIssue86() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            # The stages of our pipeline, in order:
            stages:
              - "build"
              - "unittest"
            
            ########
            # Jobs #
            ########
            
            # Build the Docker images and save them in the registry
            build:
              stage: "build"
            
            # Run unit tests on the newly built images
            unittest:
              stage: "unit<caret>test"
            """);

    var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    assertNotNull(reference);
    assertTrue(reference instanceof JobStageReferenceResolver);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertEquals("\"unittest\"", resolve.getText());
  }
}
