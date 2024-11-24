package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptReferenceResolver extends PsiReferenceBase<PsiElement> {
  public ScriptReferenceResolver(@NotNull PsiElement element, TextRange textRange) {
    super(element, textRange);
  }

  @Override
  public @Nullable PsiElement resolve() {
    final var project = myElement.getProject();
    final var scriptPath = getValue().trim();
    var localFileSystemPath = FileUtils.findVirtualFile(scriptPath, project).orElse(null);
    if (localFileSystemPath != null) {
      return PsiManager.getInstance(project).findFile(localFileSystemPath);
    }
    return null;
  }
}
