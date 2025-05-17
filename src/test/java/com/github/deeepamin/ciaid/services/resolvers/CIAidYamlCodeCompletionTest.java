package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import com.intellij.codeInsight.lookup.LookupElement;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CIAidYamlCodeCompletionTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/CodeCompletionTest");
  private List<String> completions;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    var yamlPath = getTestDirectoryName();
    completions = myFixture.getCompletionVariants(TEST_DIR_PATH + File.separator + yamlPath + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(completions);
  }

  public void testNeedsJobCompletion() {
    var expectedCompletions = List.of("build-dev", "test-job", "deploy-job");
    // there are other completions from schema as well
    assertTrue(completions.containsAll(expectedCompletions));

    myFixture.type("job");
    var completionsAfterTypingElements = myFixture.completeBasic();
    assertNotNull(completionsAfterTypingElements);
    var completionsAfterTyping = Arrays.stream(completionsAfterTypingElements).map(LookupElement::getLookupString).toList();
    assertNotNull(completionsAfterTyping);
    var expectedCompletionsAfterTyping = List.of("deploy-job", "test-job");
    var unexpectedCompletionsAfterTyping = List.of("build-dev", "build-sit");
    assertTrue(completionsAfterTyping.containsAll(expectedCompletionsAfterTyping));
    assertFalse(completionsAfterTyping.containsAll(unexpectedCompletionsAfterTyping));
  }

  public void testExtendsJobCompletion() {
    var expectedCompletions = List.of(".test-job", ".deploy-job");
    assertTrue(completions.containsAll(expectedCompletions));

    myFixture.type("tes");
    var completionsAfterTypingElements = myFixture.completeBasic();
    var completionsAfterTyping = Arrays.stream(completionsAfterTypingElements).map(LookupElement::getLookupString).toList();
    assertNotNull(completionsAfterTyping);
    var expectedCompletionsAfterTyping = List.of(".test-job");
    var unexpectedCompletionsAfterTyping = List.of(".deploy-job");
    assertTrue(completionsAfterTyping.containsAll(expectedCompletionsAfterTyping));
    assertFalse(completionsAfterTyping.containsAll(unexpectedCompletionsAfterTyping));
  }

  public void testStagesCompletion() {
    // it contains all the stages, filter the ones which are already present in stages
    var expectedCompletions = List.of("release");
    assertTrue(completions.containsAll(expectedCompletions));
  }

  public void testJobStageCompletion() {
    var expectedCompletions = List.of("build", "test", "deploy");
    var unexpectedCompletions = List.of("release");
    // there is a - stage, find where it comes from
    assertTrue(completions.containsAll(expectedCompletions));
    assertFalse(completions.containsAll(unexpectedCompletions));
  }

  public void testInputsCompletion() {
    var expectedCompletions = List.of("name", "stage", "context", "tag");
    assertTrue(completions.containsAll(expectedCompletions));
  }

  public void testRefTagsReferenceCompletion() {
    var expectedCompletions = List.of(".is_not_schedule", ".is_merge_request");
    assertTrue(completions.containsAll(expectedCompletions));
  }

  public void testRefTagsReferenceKeysCompletion() {
    var expectedCompletions = List.of("script", "before_script");
    assertTrue(completions.containsAll(expectedCompletions));
  }
}
