package com.github.deeepamin.gitlabciaid.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class ScriptReferenceResolver extends PsiReferenceBase<PsiElement> {
  private static final List<String> SCRIPT_RUNNERS = List.of("./", "python ", "python3 ");
  public ScriptReferenceResolver(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    final Project project = myElement.getProject();
    var text = myElement.getText();
    var pathBuilder = new StringBuilder();
    var basePath = project.getBasePath();
    pathBuilder.append(basePath).append(File.separator);
    SCRIPT_RUNNERS.forEach(runner -> {
      if (text.startsWith(runner)) {
        pathBuilder.append(text.substring(runner.length()));
      }
    });
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
