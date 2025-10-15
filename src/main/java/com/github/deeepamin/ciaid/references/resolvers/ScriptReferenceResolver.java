package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
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

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    String currentValue = getValue().trim();

    String newValue;
    int lastSlashIndex = Math.max(currentValue.lastIndexOf('/'), currentValue.lastIndexOf('\\'));

    if (lastSlashIndex >= 0) {
      // Has directory path, replace only the filename
      newValue = currentValue.substring(0, lastSlashIndex + 1) + newElementName;
    } else {
      // No directory path, just replace with new name
      newValue = newElementName;
    }

    return super.handleElementRename(newValue);
  }
}
