package com.github.deeepamin.ciaid.references.resolvers;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class LocalIncludeFileResolver extends IncludeFileResolver {
  public LocalIncludeFileResolver(@NotNull PsiElement element, String filePattern) {
    super(element, filePattern);
  }

  @Override
  protected ResolveResult[] getResolveResults() {
    var projectBasePath = project.getBasePath();
    if (projectBasePath != null) {
      String absolutePath = projectBasePath + File.separator + includePath;
      var localFileSystemPath = LocalFileSystem.getInstance().findFileByPath(absolutePath);
      if (localFileSystemPath != null) {
        var psiFile = PsiManager.getInstance(project).findFile(localFileSystemPath);
        if (psiFile != null) {
          return new ResolveResult[]{ new PsiElementResolveResult(psiFile) };
        }
      }
    }
    return null;
  }
}

