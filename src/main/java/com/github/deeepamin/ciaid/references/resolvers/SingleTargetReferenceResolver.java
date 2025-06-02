package com.github.deeepamin.ciaid.references.resolvers;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleTargetReferenceResolver extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
  protected final PsiElement target;

  public SingleTargetReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element);
    this.target = target;
  }

  public SingleTargetReferenceResolver(@NotNull PsiElement element, PsiElement target, TextRange textRange) {
    super(element, textRange);
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
