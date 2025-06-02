package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StagesReferenceResolver extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
  // From Stages to job level stage -> multiple jobs could have same stage so list of target
  private final List<PsiElement> targets;

  public StagesReferenceResolver(@NotNull PsiElement element, List<PsiElement> targets) {
    super(element);
    this.targets = targets;
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    if (targets != null) {
      return targets.stream()
              .map(PsiElementResolveResult::new)
              .toList()
              .toArray(ResolveResult[]::new);
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  @Override
  public @Nullable PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return myElement.getText();
  }

  public List<PsiElement> getTargets() {
    return targets;
  }

  @Override
  public Object @NotNull [] getVariants() {
    var isInputsString = GitlabCIYamlUtils.isAnInputsString(myElement.getText());
    if (isInputsString) {
      return new LookupElement[0];
    }

    var projectService = CIAidProjectService.getInstance(myElement.getProject());
    return projectService.getDataProvider().getStageNamesDefinedAtJobLevel()
            .stream()
            .map(stage -> LookupElementBuilder.create(stage)
                    .bold()
                    .withIcon(Icons.ICON_STAGE.getIcon())
                    .withTypeText(projectService.getDataProvider().getJobStageFileName(stage)))
            .toArray(LookupElement[]::new);
  }

}
