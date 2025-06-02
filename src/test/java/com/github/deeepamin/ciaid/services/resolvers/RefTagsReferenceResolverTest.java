package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;

public class RefTagsReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/ReferenceResolverTest/RefTags");

  public void testSameFileReference() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
  }

  public void testSameFileReferenceKeys() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
  }

  public void testAnotherFileReference() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE, TEST_DIR_PATH + File.separator + testDir + PIPELINE_YML_PATH);
    assertNotNull(reference);
  }

  public void testAnotherFileReferenceKeys() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE, TEST_DIR_PATH + File.separator + testDir + PIPELINE_YML_PATH);
    assertNotNull(reference);
  }

  public void testOnlyRefTagWithoutKeyReference() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            image:
              name: docker:latest
            
            stages:
              - validate
            
            .is_not_schedule:
              rules:
                - if: '$CI_PIPELINE_SOURCE != "schedule"'
                  when: always
              script:
                - echo "Not a scheduled pipeline"
            
            build-dev:
              stage: build
              image: java:latest
              tags:
                - build-dev-tag
              rules:
                - !reference [ <caret>.is_not_schedule ]
            
            
            """);
    var reference = myFixture.getReferenceAtCaretPosition(GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(reference);
    var resolve = reference.resolve();
    assertTrue(resolve instanceof YAMLKeyValue);
    assertEquals(".is_not_schedule", ((YAMLKeyValue) resolve).getKeyText());
  }
}
