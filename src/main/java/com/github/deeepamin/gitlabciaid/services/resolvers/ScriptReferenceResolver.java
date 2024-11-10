package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptReferenceResolver extends PsiReferenceBase<PsiElement> {
  public ScriptReferenceResolver(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    final Project project = myElement.getProject();
    var elementText = myElement.getText();
    FileUtils.SCRIPT_RUNNERS.stream()
            .filter(elementText::startsWith)
            .forEach(runner -> {
              var textLen = elementText.length();
              // to only underline the file name
              setRangeInElement(new TextRange(runner.length(), textLen));
            });
    var localFileSystemPath = FileUtils.getVirtualFile(elementText, project).orElse(null);
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
