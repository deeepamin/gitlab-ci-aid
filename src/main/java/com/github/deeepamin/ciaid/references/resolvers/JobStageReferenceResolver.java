package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JobStageReferenceResolver extends SingleTargetReferenceResolver {
  // From job stage to top level stages
  public JobStageReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }

  @Override
  public Object @NotNull [] getVariants() {
    var isInputsString = GitlabCIYamlUtils.isAnInputsString(myElement.getText());
    if (isInputsString) {
      return new LookupElement[0];
    }

    var projectService = CIAidProjectService.getInstance(myElement.getProject());
    return projectService
            .getStageNamesDefinedAtStagesLevel()
            .stream()
            .map(stagesItem -> LookupElementBuilder.create(stagesItem)
                    .bold()
                    .withIcon(Icons.ICON_STAGE.getIcon())
                    .withTypeText(projectService.getStagesItemFileName(stagesItem)))
            .toArray(LookupElement[]::new);
  }
}
