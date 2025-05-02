package com.github.deeepamin.ciaid.services.resolvers;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JobStageToStagesReferenceResolver extends SingleTargetReferenceResolver {
  // From job stage to top level stages
  public JobStageToStagesReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }

}
