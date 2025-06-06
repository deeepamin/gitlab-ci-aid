package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.github.deeepamin.ciaid.services.CIAidProjectService.getCIAidProjectService;
import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class NeedsReferenceResolver extends SingleTargetReferenceResolver {
  // From Needs element to Job
  public NeedsReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }

  @Override
  public Object @NotNull [] getVariants() {
    var isInputsString = InputsReferenceProvider.isAnInputsString(myElement.getText());
    if (isInputsString) {
      return new LookupElement[0];
    }

    var projectService = CIAidProjectService.getInstance(myElement.getProject());
    var allJobs = getCIAidProjectService(myElement)
            .getDataProvider()
            .getJobNames();
    var parentJob = PsiUtils.findParent(myElement, allJobs);
    List<String> filteredJobs = new ArrayList<>(allJobs);
    parentJob.ifPresent(job -> filteredJobs.remove(handleQuotedText(job.getKeyText())));

    if (filterHiddenJobs()) {
      // empty jobs with . won't run in pipeline so don't show them in needs
      filteredJobs.removeIf(job -> handleQuotedText(job).startsWith("."));
    }

    return filteredJobs.stream()
            .map(job -> LookupElementBuilder.create(job)
                    .bold()
                    .withIcon(Icons.ICON_NEEDS.getIcon())
                    .withTypeText(projectService.getDataProvider().getJobFileName(job)))
            .toArray(LookupElement[]::new);
  }

  protected boolean filterHiddenJobs() {
    return true;
  }
}
