package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

public abstract class IncludeFileResolver extends PsiPolyVariantReferenceBase<PsiElement> {
  protected PsiElement element;
  protected Project project;
  protected String includePath;

  public IncludeFileResolver(@NotNull PsiElement element, String includePath) {
    super(element);
    this.element = element;
    this.project = element.getProject();
    this.includePath = includePath;
  }

  @Override
  public @NotNull @NlsSafe String getCanonicalText() {
    return CIAidUtils.handleQuotedText(myElement.getText());
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean b) {
    var resolveResults = getResolveResults();
    if (resolveResults != null) {
      return resolveResults;
    }

    // fallback to IntelliJ Filesystem Global search
    var fallback = FileUtils.findVirtualFile(includePath, project)
            .orElse(null);
    if (fallback != null) {
      var psiFile = PsiManager.getInstance(project).findFile(fallback);
      if (psiFile != null) {
        return new ResolveResult[] { new PsiElementResolveResult(psiFile) };
      }
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  protected abstract ResolveResult[] getResolveResults();
}

