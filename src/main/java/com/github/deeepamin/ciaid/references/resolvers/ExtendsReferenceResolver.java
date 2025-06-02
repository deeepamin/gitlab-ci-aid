package com.github.deeepamin.ciaid.references.resolvers;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class ExtendsReferenceResolver extends NeedsReferenceResolver {
  // From Extends element to job
  public ExtendsReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }

  @Override
  protected boolean filterHiddenJobs() {
    // Don't filter hidden jobs in extends, because they can be used in extends
    return false;
  }
}
