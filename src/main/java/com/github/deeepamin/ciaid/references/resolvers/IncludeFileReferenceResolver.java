package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class IncludeFileReferenceResolver extends PsiPolyVariantReferenceBase<PsiElement> {
  private final String filePattern;
  private final boolean isNonLocalInclude;

  public IncludeFileReferenceResolver(@NotNull PsiElement element, String filePattern, boolean isNonLocalInclude) {
    super(element);
    this.filePattern = filePattern;
    this.isNonLocalInclude = isNonLocalInclude;
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    var project = myElement.getProject();
    if (CIAidUtils.containsWildcardWithYmlExtension(filePattern)) {
      var files = FileUtils.findVirtualFilesByGlob(filePattern, project);
      var psiManager = PsiManager.getInstance(project);
      return files.stream()
        .map(psiManager::findFile)
        .filter(java.util.Objects::nonNull)
        .map(PsiElementResolveResult::new)
        .toArray(ResolveResult[]::new);
    } else {
      String absolutePath = null;
      if (isNonLocalInclude) {
        absolutePath = filePattern;
      } else {
        var projectBasePath = project.getBasePath();
        if (projectBasePath != null) {
          absolutePath = projectBasePath + File.separator + filePattern;
        }
      }

      var psiFile = getPsiFileResolveResult(absolutePath, project);
      if (psiFile != null) {
        return psiFile;
      }
    }

    // fallback to IntelliJ Filesystem Global search (only if not found in project)
    var fallback = FileUtils.findVirtualFile(filePattern, project).orElse(null);
    if (fallback != null) {
      var psiFile = PsiManager.getInstance(project).findFile(fallback);
      if (psiFile != null) {
        return new ResolveResult[] { new PsiElementResolveResult(psiFile) };
      }
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return CIAidUtils.handleQuotedText(myElement.getText());
  }

  private ResolveResult @Nullable [] getPsiFileResolveResult(String absolutePath, Project project) {
    if (absolutePath == null) {
      return null;
    }
    var localFileSystemPath = LocalFileSystem.getInstance().findFileByPath(absolutePath);
    if (localFileSystemPath != null) {
      var psiFile = PsiManager.getInstance(project).findFile(localFileSystemPath);
      if (psiFile != null) {
        return new ResolveResult[]{new PsiElementResolveResult(psiFile)};
      }
    }
    return null;
  }
}
