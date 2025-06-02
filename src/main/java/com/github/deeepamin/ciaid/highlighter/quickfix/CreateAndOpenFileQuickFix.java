package com.github.deeepamin.ciaid.highlighter.quickfix;

import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class CreateAndOpenFileQuickFix implements LocalQuickFix {
  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    var element = descriptor.getPsiElement();
    if (!YamlUtils.isYamlTextElement(element)) {
      return;
    }
    var scriptPath = CIAidUtils.handleQuotedText(element.getText());
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
