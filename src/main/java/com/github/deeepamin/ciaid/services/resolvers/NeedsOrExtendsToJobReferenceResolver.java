package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;
import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.getGitlabCIYamlProjectService;

public class NeedsOrExtendsToJobReferenceResolver extends SingleTargetReferenceResolver {
  // From Needs element to Job or Extends element to job
  public NeedsOrExtendsToJobReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }

  @Override
  public Object @NotNull [] getVariants() {
    var isInputsString = GitlabCIYamlUtils.isAnInputsString(myElement.getText());
    if (isInputsString) {
      return new LookupElement[0];
    }

    var projectService = CIAidProjectService.getInstance(myElement.getProject());
    var allJobs = getGitlabCIYamlProjectService(myElement).getJobNames();
    var parentJob = PsiUtils.findParent(myElement, allJobs);
    List<String> filteredJobs = new ArrayList<>(allJobs);
    parentJob.ifPresent(job -> filteredJobs.remove(handleQuotedText(job.getKeyText())));

    boolean isChildOfNeedsElement = PsiUtils.isNeedsElement(myElement);
    if (isChildOfNeedsElement) {
      // empty jobs with . won't run in pipeline so don't show them in needs
      filteredJobs.removeIf(job -> handleQuotedText(job).startsWith("."));
    }

    return filteredJobs.stream()
            .map(job -> LookupElementBuilder.create(job)
                    .bold()
                    .withIcon(Icons.ICON_NEEDS.getIcon())
                    .withTypeText(projectService.getJobFileName(job)))
            .toArray(LookupElement[]::new);
  }
}
