package com.github.deeepamin.ciaid.references.resolvers;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class DependenciesReferenceResolver extends NeedsReferenceResolver {
  // From Extends element to job
  public DependenciesReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }
}
