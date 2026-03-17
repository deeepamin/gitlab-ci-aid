package com.github.deeepamin.ciaid.highlighter.quickfix;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

public class FixExposeAsQuickFix implements LocalQuickFix {
  // Replace any character that is NOT a letter, digit, hyphen, underscore, or space
  private static final String INVALID_CHARS_PATTERN = "[^\\w\\- ]";

  @Override
  public @NotNull String getName() {
    return CIAidBundle.message("inspections.gitlab.ci.fix.expose-as");
  }

  @Override
  public @NotNull String getFamilyName() {
    return getName();
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    var element = descriptor.getPsiElement();
    if (!YamlUtils.isYamlTextElement(element)) {
      return;
    }
    var originalText = CIAidUtils.handleQuotedText(element.getText());
    var fixedText = originalText.replaceAll(INVALID_CHARS_PATTERN, "_");
    if (fixedText.equals(originalText)) {
      return;
    }
    if (element instanceof YAMLPlainTextImpl plainText) {
      plainText.updateText(fixedText);
    } else if (element instanceof YAMLQuotedText quotedText) {
      quotedText.updateText(fixedText);
    }
  }
}

