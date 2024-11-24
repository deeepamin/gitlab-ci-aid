package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.github.deeepamin.gitlabciaid.utils.ReferenceUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IncludeFileReferenceResolver extends PsiReferenceBase<PsiElement> {

  public IncludeFileReferenceResolver(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    final Project project = myElement.getProject();
    var text = ReferenceUtils.handleQuotedText(myElement.getText());
    var localFileSystemPath = FileUtils.findVirtualFile(text, project).orElse(null);
    if (localFileSystemPath != null) {
      return PsiManager.getInstance(project).findFile(localFileSystemPath);
    }
    return null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return ReferenceUtils.handleQuotedText(myElement.getText());
  }
}
