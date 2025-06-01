package com.github.deeepamin.ciaid.services.inspections;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.NEEDS;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.NEEDS_POSSIBLE_CHILD_KEYWORDS;
import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.getGitlabCIYamlProjectService;

public class CIAidGitLabYamlUndefinedNeedsInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (GitlabCIYamlUtils.hasGitlabYamlFile(element) && YamlUtils.isYamlTextElement(element)) {
          var isChildOfNeeds = PsiUtils.isChild(element, List.of(NEEDS));
          if (isChildOfNeeds) {
            var notOtherNeeds =  !PsiUtils.isChild(element, NEEDS_POSSIBLE_CHILD_KEYWORDS);
            if (notOtherNeeds) {
              var allJobs = getGitlabCIYamlProjectService(element).getJobNames();
              var jobName = CIAidUtils.handleQuotedText(element.getText());
              var isInputsString = GitlabCIYamlUtils.isAnInputsString(jobName);
              if (isInputsString) {
                return;
              }
              if (!allJobs.contains(jobName)) {
                var inspectionText = CIAidBundle.message("inspections.gitlab.ci.need-job-undefined", jobName);
                holder.registerProblem(element, inspectionText);
              }
            }
          }
        }
      }
    };
  }
}
