package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.model.CIAidYamlData;
import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.getGitlabCIYamlProjectService;

public class NeedsOrExtendsToJobReferenceResolver extends SingleTargetReferenceResolver {
  // From Needs element to Job or Extends element to job
  public NeedsOrExtendsToJobReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }

  @Override
  public Object @NotNull [] getVariants() {
    var isInputsString = GitlabCIYamlUtils.getInputNameFromInputsString(myElement.getText()) != null;
    if (isInputsString) {
      return new LookupElement[0];
    }

    var projectService = CIAidProjectService.getInstance(myElement.getProject());
    var allJobs = getGitlabCIYamlProjectService(myElement).getJobNames();
    var parentJob = PsiUtils.findParent(myElement, allJobs);
    List<String> filteredJobs = new ArrayList<>(allJobs);
    parentJob.ifPresent(job -> filteredJobs.remove(job.getKeyText()));

    boolean isChildOfNeedsElement = PsiUtils.isNeedsElement(myElement);
    if (isChildOfNeedsElement) {
      // empty jobs with . won't run in pipeline so don't show them in needs
      filteredJobs.removeIf(job -> job.startsWith("."));
    }
    BiPredicate<Map.Entry<VirtualFile, CIAidYamlData>, String> jobFilterPredicate = (entry, job) -> entry.getValue().getJobElements()
            .stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .anyMatch(pointer -> pointer.getElement().getText().equals(job));

    return filteredJobs.stream()
            .map(job -> LookupElementBuilder.create(job)
                    .bold()
                    .withIcon(Icons.ICON_NEEDS.getIcon())
                    .withTypeText(projectService.getFileName(myElement.getProject(), entry -> jobFilterPredicate.test(entry, job))))
            .toArray(LookupElement[]::new);
  }
}
