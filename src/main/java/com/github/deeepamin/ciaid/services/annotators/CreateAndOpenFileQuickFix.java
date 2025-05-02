package com.github.deeepamin.ciaid.services.annotators;

import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.ReferenceUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class CreateAndOpenFileQuickFix implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance(CreateAndOpenFileQuickFix.class);

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    var element = descriptor.getPsiElement();
    if (!PsiUtils.isYamlTextElement(element)) {
      return;
    }
    var scriptPath = ReferenceUtils.handleQuotedText(element.getText());
    Path scriptAbsolutePath = null;
    try {
      scriptAbsolutePath = FileUtils.getFilePath(scriptPath, project);
    } catch (InvalidPathException ipX) {
      LOG.error("Error while getting file path " + scriptPath, ipX);
    }
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
