package com.github.deeepamin.gitlabciaid.services.annotators;

import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.github.deeepamin.gitlabciaid.utils.ReferenceUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class CreateAndOpenFileQuickFix implements LocalQuickFix {
  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    var element = descriptor.getPsiElement();
    if (!PsiUtils.isYamlTextElement(element)) {
      return;
    }
    var scriptPath = ReferenceUtils.handleQuotedText(element.getText());
    var scriptAbsolutePath = FileUtils.getFilePath(scriptPath, project);
    if (scriptAbsolutePath == null) {
      return;
    }
    FileUtils.createFile(scriptAbsolutePath);
    FileUtils.refreshFileAndOpenInEditor(scriptAbsolutePath, project);
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
