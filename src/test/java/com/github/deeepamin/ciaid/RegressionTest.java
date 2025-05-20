package com.github.deeepamin.ciaid;

import com.github.deeepamin.ciaid.services.resolvers.JobStageToStagesReferenceResolver;

public class RegressionTest extends BaseTest {
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
    assertTrue(reference instanceof JobStageToStagesReferenceResolver);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertEquals("\"unittest\"", resolve.getText());
  }
}
