package com.github.deeepamin.ciaid.completion;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Suppresses all code completions (including schema-based ones) when the cursor
 * is inside a YAML comment in GitLab CI files.
 * <p>
 * Registered with {@code order="first"} so it runs before the JSON Schema
 * completion contributor and can call {@link CompletionResultSet#stopHere()}.
 */
public class CIAidYamlCommentCompletionContributor extends CompletionContributor {

  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    var file = parameters.getOriginalFile();
      if (!CIAidProjectService.isValidGitlabCIYamlFile(file.getVirtualFile())) {
      return;
    }
    if (isInsideComment(parameters.getPosition())) {
      result.stopHere();
      return;
    }
    super.fillCompletionVariants(parameters, result);
  }

  private static boolean isInsideComment(@NotNull PsiElement element) {
    PsiElement current = element;
    while (current != null && !(current instanceof PsiFile)) {
      if (current instanceof PsiComment) {
        return true;
      }
      current = current.getParent();
    }
    return false;
  }
}

