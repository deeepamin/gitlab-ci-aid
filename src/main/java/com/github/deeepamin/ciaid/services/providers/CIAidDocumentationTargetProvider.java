package com.github.deeepamin.ciaid.services.providers;

import com.github.deeepamin.ciaid.model.InputDocumentationTarget;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CIAidDocumentationTargetProvider implements PsiDocumentationTargetProvider {
  @Override
  public @Nullable DocumentationTarget documentationTarget(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
    if (originalElement == null) {
      return null;
    }
    var isGitlabCIYaml = GitlabCIYamlUtils.isValidGitlabCIYamlFile(element.getContainingFile().getVirtualFile());
    if (isGitlabCIYaml) {
      var elementText = originalElement.getText();
      var inputsText = GitlabCIYamlUtils.getInputNameFromInputsString(elementText);
      if (inputsText != null) {
        return new InputDocumentationTarget(element, originalElement);
      }
    }
    return null;
  }
}
