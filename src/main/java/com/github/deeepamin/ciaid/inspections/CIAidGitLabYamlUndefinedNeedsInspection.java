package com.github.deeepamin.ciaid.inspections;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.services.CIAidProjectService.getCIAidProjectService;

public class CIAidGitLabYamlUndefinedNeedsInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (CIAidProjectService.hasGitlabYamlFile(element) && YamlUtils.isYamlTextElement(element)) {
          var isChildOfNeeds = PsiUtils.isChild(element, List.of(NEEDS));
          if (isChildOfNeeds) {
            var notOtherNeeds =  !PsiUtils.isChild(element, NEEDS_POSSIBLE_CHILD_KEYWORDS);
            if (notOtherNeeds) {
              var allJobs = getCIAidProjectService(element)
                      .getDataProvider()
                      .getJobNames();
              var jobName = CIAidUtils.handleQuotedText(element.getText());
              var isInputsString = InputsReferenceProvider.isAnInputsString(jobName);
              if (isInputsString) {
                return;
              }
              if (!allJobs.contains(jobName)) {
                var inspectionText = CIAidBundle.message("inspections.gitlab.ci.need-job-undefined", element.getText());
                holder.registerProblem(element, inspectionText);
              }
            }
          }
        }
      }
    };
  }
}
