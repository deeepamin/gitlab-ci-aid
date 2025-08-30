package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

public class IncludeFileReferenceResolver extends PsiPolyVariantReferenceBase<PsiElement> {
  private final String filePattern;

  public IncludeFileReferenceResolver(@NotNull PsiElement element, String filePattern) {
    super(element);
    this.filePattern = filePattern;
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    var project = myElement.getProject();
    if (CIAidUtils.containsWildcard(filePattern)) {
      var files = FileUtils.findVirtualFilesByGlob(filePattern, project);
      var psiManager = PsiManager.getInstance(project);
      return files.stream()
        .map(psiManager::findFile)
        .filter(java.util.Objects::nonNull)
        .map(PsiElementResolveResult::new)
        .toArray(ResolveResult[]::new);
    } else {
      var localFileSystemPath = LocalFileSystem.getInstance().findFileByPath(filePattern);
      if (localFileSystemPath != null) {
        var psiFile = PsiManager.getInstance(project).findFile(localFileSystemPath);
        if (psiFile != null) {
          return new ResolveResult[] { new PsiElementResolveResult(psiFile) };
        }
      }
      // fallback to IntelliJ Filesystem Global search
      var fallback = FileUtils.findVirtualFile(filePattern, project).orElse(null);
      if (fallback != null) {
        var psiFile = PsiManager.getInstance(project).findFile(fallback);
        if (psiFile != null) {
          return new ResolveResult[] { new PsiElementResolveResult(psiFile) };
        }
      }
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return CIAidUtils.handleQuotedText(myElement.getText());
  }
}
