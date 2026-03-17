package com.github.deeepamin.ciaid.inspections;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.highlighter.quickfix.FixExposeAsQuickFix;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class CIAidGitLabYamlExposeAsInvalidInspection extends LocalInspectionTool {
  // GitLab only allows letters, digits, '-', '_' and spaces in expose_as
  static final Pattern VALID_EXPOSE_AS_PATTERN = Pattern.compile("^[\\w\\- ]+$");

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (!CIAidProjectService.hasGitlabYamlFile(element) || !YamlUtils.isYamlTextElement(element)) {
          return;
        }
        var isChildOfExposeAs = PsiUtils.isChild(element, List.of(EXPOSE_AS));
        if (!isChildOfExposeAs) {
          return;
        }
        var isChildOfArtifacts = PsiUtils.isChild(element, List.of(ARTIFACTS));
        if (!isChildOfArtifacts) {
          return;
        }
        var value = handleQuotedText(element.getText());
        if (value.isEmpty()) {
          return;
        }
        if (!VALID_EXPOSE_AS_PATTERN.matcher(value).matches()) {
          var inspectionText = CIAidBundle.message("inspections.gitlab.ci.expose-as.invalid", value);
          var quickFix = new FixExposeAsQuickFix();
          holder.registerProblem(element, inspectionText, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, quickFix);
        }
      }
    };
  }
}

