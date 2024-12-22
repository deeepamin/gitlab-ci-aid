package com.github.deeepamin.gitlabciaid.services.contributors;

import com.github.deeepamin.gitlabciaid.BaseTest;

import java.util.List;

public class GitlabCIYamlCodeContributorTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/CodeCompletionTest";
  private List<String> completions;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    var yamlPath = getTestDirectoryName();
    completions = myFixture.getCompletionVariants(TEST_DIR_PATH + "/" + yamlPath + "/" + GITLAB_CI_DEFAULT_YAML_FILE);
    assertNotNull(completions);
  }

  public void testNeedsJobCompletion() {
    var expectedCompletions = List.of("build-dev", "test-job", "deploy-job");
    // there are other completions from schema as well
    assertTrue(completions.containsAll(expectedCompletions));

    myFixture.type("bui");
    var completionsAfterTyping = myFixture.getLookupElementStrings();
    assertNotNull(completionsAfterTyping);
    var expectedCompletionsAfterTyping = List.of("build-dev");
    var unexpectedCompletionsAfterTyping = List.of("test-job", "deploy-job");
    assertTrue(completionsAfterTyping.containsAll(expectedCompletionsAfterTyping));
    assertFalse(completionsAfterTyping.containsAll(unexpectedCompletionsAfterTyping));
  }

  public void testExtendsJobCompletion() {
    var expectedCompletions = List.of(".test-job", ".deploy-job");
    // there are other completions from schema as well
    assertTrue(completions.containsAll(expectedCompletions));

    myFixture.type(".test");
    var completionsAfterTyping = myFixture.getLookupElementStrings();
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
}
