package com.github.deeepamin.ciaid.services.injectors;

import com.github.deeepamin.ciaid.BaseTest;

public class GitlabCIYamlScriptInjectorTest extends BaseTest {
  private static final String LITERAL_SCRIPT_TEXT = """
          script: |
              <inject descr="null"><info descr="null"><info descr="null">echo</info></info> <info descr="null">Tag</info> <info descr="null">release</info>
              <info descr="null"><info descr="null">git</info></info> <info descr="null">checkout</info> "$CI_COMMIT_REF_NAME"
              <info descr="null"><info descr="null">TAG_NAME</info></info>="v$CUR_VERSION"
              <info descr="null"><info descr="null">git</info></info> <info descr="null">tag</info> $TAG_NAME</inject>""";

  private static final String FOLDING_SCRIPT_TEXT = """
          script: >
              <inject descr="null"><info descr="null"><info descr="null">echo</info></info> <info descr="null">Tag</info> <info descr="null">release</info>
              <info descr="null"><info descr="null">git</info></info> <info descr="null">checkout</info> "$CI_COMMIT_REF_NAME"
              <info descr="null"><info descr="null">TAG_NAME</info></info>="v$CUR_VERSION"
              <info descr="null"><info descr="null">git</info></info> <info descr="null">tag</info> $TAG_NAME</inject>""";

  public void testMultiLineLiteralInjection() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, LITERAL_SCRIPT_TEXT);
    myFixture.testHighlighting(true, true, false);
  }

  public void testMultiLineFoldingInjection() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, FOLDING_SCRIPT_TEXT);
    myFixture.testHighlighting(true, true, false);
  }
}
