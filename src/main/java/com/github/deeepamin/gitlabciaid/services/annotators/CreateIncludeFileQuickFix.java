package com.github.deeepamin.gitlabciaid.services.annotators;

import com.github.deeepamin.gitlabciaid.GitlabCIAidBundle;
import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CreateIncludeFileQuickFix implements LocalQuickFix {
  @Override
  public @IntentionName @NotNull String getName() {
    return GitlabCIAidBundle.message("annotator.gitlabciaid.create-include-file");
  }

  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return GitlabCIAidBundle.message("annotator.gitlabciaid.create-include-file");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    var element = descriptor.getPsiElement();
    if (!PsiUtils.isYamlTextElement(element)) {
      return;
    }
    var includePath = element.getText();
    var includeAbsolutePath = FileUtils.getFilePath(includePath, project);
    FileUtils.createFile(includeAbsolutePath);
    FileUtils.refreshFileAndOpenInEditor(includeAbsolutePath, project);
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
