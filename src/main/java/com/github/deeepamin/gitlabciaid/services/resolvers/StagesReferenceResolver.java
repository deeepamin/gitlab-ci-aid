package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StagesReferenceResolver extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
  private final PsiElement target;

  // From one stage to top level stages
  public StagesReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element);
    this.target = target;
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    if (target != null) {
      return new ResolveResult[]{new PsiElementResolveResult(target)};
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
}
