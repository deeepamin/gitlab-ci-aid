package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class IncludeFileReferenceResolver extends PsiReferenceBase<PsiElement> {

  public IncludeFileReferenceResolver(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    final Project project = myElement.getProject();
    var text = myElement.getText();
    var pathBuilder = new StringBuilder();
    var basePath = project.getBasePath();
    pathBuilder.append(basePath);
    if (!text.startsWith(File.separator)) {
      pathBuilder.append(File.separator);
    }
    pathBuilder.append(text);
    var localFileSystemPath = LocalFileSystem.getInstance().findFileByPath(pathBuilder.toString());
    if (localFileSystemPath != null) {
      return PsiManager.getInstance(project).findFile(localFileSystemPath);
    }
    return null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return myElement.getText();
  }
}
