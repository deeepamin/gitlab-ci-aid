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

public class CreateScriptQuickFix implements LocalQuickFix {
  @Override
  public @IntentionName @NotNull String getName() {
    return GitlabCIAidBundle.message("annotator.gitlabciaid.create-script");
  }

  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return GitlabCIAidBundle.message("annotator.gitlabciaid.create-script");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    var element = descriptor.getPsiElement();
    if (!PsiUtils.isYamlTextElement(element)) {
      return;
    }
    var scriptPath = element.getText();
    var scriptAbsolutePath = FileUtils.getFilePath(scriptPath, project);
    FileUtils.createFile(scriptAbsolutePath);
    FileUtils.refreshFileAndOpenInEditor(scriptAbsolutePath, project);
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
