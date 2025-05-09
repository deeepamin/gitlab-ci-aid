package com.github.deeepamin.ciaid.services.annotators;

import com.github.deeepamin.ciaid.BaseTest;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.List;

public class GitlabCIYamlAnnotatorTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/AnnotatorTest");
  private VirtualFile ciYamlFile;
  private VirtualFile nonGitlabYamlFile;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH + File.separator + getTestDirectoryName(), "");
    ciYamlFile = getGitlabCIYamlFile(rootDir);
    nonGitlabYamlFile = getYamlFile(rootDir, "build.yml");
  }

  public void testCorrectHighlighting() {
    myFixture.configureByFile(TEST_DIR_PATH + File.separator + getTestDirectoryName() + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
    assertEquals(6, highlightInfos.size());
    List<String> actualHighlighters = highlightInfos.stream()
            .map(HighlightInfo::getText)
            .toList();

    // can't test with testHighlighting as job name gets highlighted, and if highlight attribute is added the job name doesn't get read
    List<String> expectedHighlighters = List.of("<text_attr descr=\"null\">validate</text_attr>",
            "<text_attr descr=\"null\">build</text_attr>",
            "<text_attr descr=\"null\">test</text_attr>",
            "<text_attr descr=\"null\">deploy</text_attr>",
            "build-dev",
            "<text_attr descr=\"null\">build</text_attr>");
    assertEquals(expectedHighlighters, actualHighlighters);
  }

  public void testCorrectScriptInjection() {
    myFixture.configureByFile(TEST_DIR_PATH + File.separator + getTestDirectoryName() + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
    List<String> actualHighlighters = highlightInfos.stream()
            .map(HighlightInfo::getText)
            .toList();

    // can't test with testHighlighting as job name gets highlighted, and if highlight attribute is added the job name doesn't get read
    String expectedHighlighter = "echo \"This is injection testing\"";
    assertTrue(actualHighlighters.contains(expectedHighlighter));
  }

  public void testUndefinedStage() {
    myFixture.testHighlighting(true, false, true, ciYamlFile);
  }

  public void testUndefinedNeedsJob() {
    myFixture.testHighlighting(true, false, true, ciYamlFile);
  }

  public void testUnknownScript() {
    myFixture.configureByFile(TEST_DIR_PATH + File.separator + getTestDirectoryName() + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
    assertEquals(6, highlightInfos.size());
    assertEquals("Script './build-dev.sh' is not available on path", highlightInfos.get(3).getDescription());
  }

  public void testCreateScriptQuickFix() {
    List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes(TEST_DIR_PATH + File.separator + getTestDirectoryName() + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertEquals(1, allQuickFixes.size());
    assertEquals("Create script", allQuickFixes.getFirst().getText());
  }

  public void testNonGitlabCIYamlFile() {
    myFixture.testHighlighting(true, true, true, nonGitlabYamlFile);
  }

  public void testCreateIncludeFileQuickFix() {
    List<IntentionAction> allQuickFixes = myFixture.getAllQuickFixes(TEST_DIR_PATH + File.separator + getTestDirectoryName() + File.separator + GITLAB_CI_DEFAULT_YAML_FILE);
    assertEquals(1, allQuickFixes.size());
    assertEquals("Create include file", allQuickFixes.getFirst().getText());
  }

  public void testIncludeFileHighlighting() {
    myFixture.testHighlighting(true, false, true, ciYamlFile);
  }
}
