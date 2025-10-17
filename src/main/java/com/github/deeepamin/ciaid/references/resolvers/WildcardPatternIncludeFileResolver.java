package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

public class WildcardPatternIncludeFileResolver extends IncludeFileResolver {
  public WildcardPatternIncludeFileResolver(@NotNull PsiElement element, String filePattern) {
    super(element, filePattern);
  }

  @Override
  protected ResolveResult[] getResolveResults() {
    var files = FileUtils.findVirtualFilesByGlob(includePath, project);
    var psiManager = PsiManager.getInstance(project);
    var results = files.stream()
            .map(psiManager::findFile)
            .filter(java.util.Objects::nonNull)
            .map(PsiElementResolveResult::new)
            .toArray(ResolveResult[]::new);

    return results.length > 0 ? results : null;
  }
}

