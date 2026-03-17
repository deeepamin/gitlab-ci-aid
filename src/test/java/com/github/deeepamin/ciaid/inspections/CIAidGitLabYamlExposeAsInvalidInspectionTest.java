package com.github.deeepamin.ciaid.inspections;

import com.github.deeepamin.ciaid.BaseTest;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.HighlightSeverity;

import java.util.List;

public class CIAidGitLabYamlExposeAsInvalidInspectionTest extends BaseTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new CIAidGitLabYamlExposeAsInvalidInspection());
  }

  public void testExposeAsWithDotIsInvalid() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            build:
              artifacts:
                paths:
                  - foo.json
                expose_as: foo.json
            """);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    var errors = highlights.stream()
            .filter(h -> h.getSeverity().equals(HighlightSeverity.ERROR))
            .filter(h -> h.getDescription() != null && h.getDescription().contains("can contain only"))
            .toList();
    assertEquals(1, errors.size());
    assertTrue(errors.getFirst().getDescription().contains("foo.json"));
  }

  public void testExposeAsWithSpecialCharsIsInvalid() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            build:
              artifacts:
                paths:
                  - report.html
                expose_as: "report@v2!.html"
            """);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    var errors = highlights.stream()
            .filter(h -> h.getSeverity().equals(HighlightSeverity.ERROR))
            .filter(h -> h.getDescription() != null && h.getDescription().contains("can contain only"))
            .toList();
    assertEquals(1, errors.size());
  }

  public void testExposeAsWithValidValueNoWarning() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            build:
              artifacts:
                paths:
                  - foo.json
                expose_as: foo_json
            """);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    var errors = highlights.stream()
            .filter(h -> h.getSeverity().equals(HighlightSeverity.ERROR))
            .filter(h -> h.getDescription() != null && h.getDescription().contains("can contain only"))
            .toList();
    assertTrue(errors.isEmpty());
  }

  public void testExposeAsWithHyphenSpaceUnderscoreIsValid() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            build:
              artifacts:
                paths:
                  - report.html
                expose_as: "My Report - build_output"
            """);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    var errors = highlights.stream()
            .filter(h -> h.getSeverity().equals(HighlightSeverity.ERROR))
            .filter(h -> h.getDescription() != null && h.getDescription().contains("can contain only"))
            .toList();
    assertTrue(errors.isEmpty());
  }

  public void testQuickFixReplacesInvalidChars() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            build:
              artifacts:
                paths:
                  - foo.json
                expose_as: foo.json
            """);
    List<IntentionAction> quickFixes = myFixture.getAllQuickFixes();
    var exposeAsFix = quickFixes.stream()
            .filter(fix -> fix.getText().equals("Replace invalid characters with '_'"))
            .findFirst();
    assertTrue("Quick fix should be available", exposeAsFix.isPresent());
    myFixture.launchAction(exposeAsFix.get());
    myFixture.checkResult("""
            build:
              artifacts:
                paths:
                  - foo.json
                expose_as: foo_json
            """);
  }

  public void testQuickFixReplacesMultipleInvalidChars() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            deploy:
              artifacts:
                paths:
                  - output/report.v2.html
                expose_as: "report@v2!.html"
            """);
    List<IntentionAction> quickFixes = myFixture.getAllQuickFixes();
    var exposeAsFix = quickFixes.stream()
            .filter(fix -> fix.getText().equals("Replace invalid characters with '_'"))
            .findFirst();
    assertTrue("Quick fix should be available", exposeAsFix.isPresent());
    myFixture.launchAction(exposeAsFix.get());
    myFixture.checkResult("""
            deploy:
              artifacts:
                paths:
                  - output/report.v2.html
                expose_as: "report_v2__html"
            """);
  }
}
