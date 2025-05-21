package com.github.deeepamin.ciaid.services.contributors;

import com.github.deeepamin.ciaid.BaseTest;
import com.intellij.codeInsight.lookup.LookupElement;

import java.util.Arrays;
import java.util.List;

public class CIAidShellCodeContributorTest extends BaseTest {
  public void testYamlElementCodeCompletions() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            image:
              name: docker:latest
            
            variables:
              TEST_VAR1: TEST_VALUE1
              TEST_VAR2: TEST_VALUE2
            
            include:
              - /pipeline.yml
            
            stages:
              - validate
              - build
           
            script: echo ${<caret>}
            """);
    var completions = Arrays.stream(myFixture.completeBasic())
            .map(LookupElement::getLookupString)
            .filter(lookupString -> !lookupString.isBlank())
            .toList();
    assertNotNull(completions);
    var expectedCompletions = List.of("TEST_VAR1", "TEST_VAR2");
    assertTrue(completions.containsAll(expectedCompletions));
  }

  public void testShellScriptInjectedCodeCompletions() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            image:
              name: docker:latest
            
            variables:
              TEST_VAR1: TEST_VALUE1
              TEST_VAR2: TEST_VALUE2
            
            include:
              - /pipeline.yml
            
            stages:
              - validate
              - build
           
            script:
              - echo ${<caret>}
            """);
    var completions = Arrays.stream(myFixture.completeBasic())
            .map(LookupElement::getLookupString)
            .filter(lookupString -> !lookupString.isBlank())
            .toList();
    assertNotNull(completions);
    var expectedCompletions = List.of("TEST_VAR1", "TEST_VAR2");
    assertTrue(completions.containsAll(expectedCompletions));
  }

}
