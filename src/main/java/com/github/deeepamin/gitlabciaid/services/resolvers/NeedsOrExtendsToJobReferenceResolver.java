package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class NeedsOrExtendsToJobReferenceResolver extends SingleTargetReferenceResolver {
  // From Needs element to Job or Extends element to job
  public NeedsOrExtendsToJobReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }
}
