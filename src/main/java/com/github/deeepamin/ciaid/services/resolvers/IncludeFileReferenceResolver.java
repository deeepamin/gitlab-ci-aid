package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class IncludeFileReferenceResolver extends PsiReferenceBase<PsiElement> {
  private final Path filePath;

  public IncludeFileReferenceResolver(@NotNull PsiElement element, Path filePath) {
    super(element);
    this.filePath = filePath;
  }

  @Override
  public @Nullable PsiElement resolve() {
    if (filePath != null) {
      var localFileSystemPath = LocalFileSystem.getInstance().findFileByPath(filePath.toString());
      if (localFileSystemPath != null) {
        return PsiManager.getInstance(myElement.getProject()).findFile(localFileSystemPath);
      }
    }
    // fallback to Intellij Filesystem Global search in case the path doesn't resolve to a file in the project
    var localFileSystemPath = FileUtils.findVirtualFile(myElement.getText(), myElement.getProject()).orElse(null);
    if (localFileSystemPath != null) {
      return PsiManager.getInstance(myElement.getProject()).findFile(localFileSystemPath);
    }
    return null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return CIAidUtils.handleQuotedText(myElement.getText());
  }
}
