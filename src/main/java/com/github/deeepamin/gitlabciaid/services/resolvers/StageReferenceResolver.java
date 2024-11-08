package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StageReferenceResolver extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
  private final List<PsiElement> targets;

  public StageReferenceResolver(@NotNull PsiElement element, List<PsiElement> targets) {
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
}
