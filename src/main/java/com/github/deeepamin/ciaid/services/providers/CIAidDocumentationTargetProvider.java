package com.github.deeepamin.ciaid.services.providers;

import com.github.deeepamin.ciaid.services.targets.InputDocumentationTarget;
import com.github.deeepamin.ciaid.services.targets.MultipleInputsDocumentationTarget;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.DocumentationTargetProvider;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CIAidDocumentationTargetProvider implements DocumentationTargetProvider {

  @Override
  public @NotNull List<? extends @NotNull DocumentationTarget> documentationTargets(@NotNull PsiFile file, int offset) {
    var documentationTargets = new ArrayList<DocumentationTarget>();
    if (file.getVirtualFile() != null) {
      var isGitlabCIYaml = GitlabCIYamlUtils.isValidGitlabCIYamlFile(file.getVirtualFile());
      if (isGitlabCIYaml) {
        var elementAtOffset = file.findElementAt(offset);
        if (elementAtOffset != null) {
          var elementText = elementAtOffset.getText();
          var inputNamesAndOffsets = GitlabCIYamlUtils.getInputNames(elementText);
          if (inputNamesAndOffsets != null && !inputNamesAndOffsets.isEmpty()) {
            if (inputNamesAndOffsets.size() == 1) {
              documentationTargets.add(new InputDocumentationTarget(file, elementAtOffset));
            } else {
              inputNamesAndOffsets.forEach(inputWithTextRange -> {
                var target = new MultipleInputsDocumentationTarget(file, elementAtOffset, inputWithTextRange);
                documentationTargets.add(target);
              });
            }
          }

        }
      }
    }
    return documentationTargets;
  }
}
