package com.github.deeepamin.ciaid.references.resolvers;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

// for remote files like component, project, remote, template includes
// not naming it remote to avoid confusion with remote as in remote include
public class NonLocalIncludeFileResolver extends IncludeFileResolver {
  public NonLocalIncludeFileResolver(@NotNull PsiElement element, String filePattern) {
    super(element, filePattern);
  }

  @Override
  protected ResolveResult[] getResolveResults() {
    var localFileSystemPath = LocalFileSystem.getInstance().findFileByPath(includePath);
    if (localFileSystemPath != null) {
      var psiFile = PsiManager.getInstance(project).findFile(localFileSystemPath);
      if (psiFile != null) {
        return new ResolveResult[]{ new PsiElementResolveResult(psiFile) };
      }
    }
    return null;
  }
}

