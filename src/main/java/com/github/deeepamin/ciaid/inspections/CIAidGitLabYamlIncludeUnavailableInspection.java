package com.github.deeepamin.ciaid.inspections;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.highlighter.quickfix.CreateIncludeFileQuickFix;
import com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class CIAidGitLabYamlIncludeUnavailableInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (CIAidProjectService.hasGitlabYamlFile(element) && YamlUtils.isYamlTextElement(element)) {
          var isChildOfIncludeElement = PsiUtils.isChild(element, List.of(INCLUDE));
          if (isChildOfIncludeElement) {
            var isNonLocalInclude = PsiUtils.isChild(element, NON_LOCAL_INCLUDE_KEYWORDS);
            if (!isNonLocalInclude) {
              var filePath = handleQuotedText(element.getText());
              var inputsFilePathString = InputsReferenceProvider.isAnInputsString(filePath);
              if (inputsFilePathString) {
                return;
              }
              var project = element.getProject();
              var pathContainsWildcard = CIAidUtils.containsWildcard(filePath);
              if (pathContainsWildcard) {
                var includeFiles = FileUtils.findVirtualFilesByGlob(filePath, project);
                if (includeFiles.isEmpty()) {
                  registerProblemAndQuickFix(element, holder);
                } else {
                  return;
                }
              }
              var includeVirtualFile = FileUtils.findVirtualFile(filePath, project).orElse(null);
              var isRemoteInclude = CIAidUtils.isValidUrl(filePath);
              if (includeVirtualFile == null) {
                if (!isRemoteInclude) {
                  registerProblemAndQuickFix(element, holder);
                }
              }
            }
          }
        }
      }
    };
  }

  private void registerProblemAndQuickFix(@NotNull PsiElement element, @NotNull ProblemsHolder holder) {
    var inspectionText = CIAidBundle.message("inspections.gitlab.ci.include-not-found", handleQuotedText(element.getText()));
    var quickFix = new CreateIncludeFileQuickFix();
    holder.registerProblem(element, inspectionText, quickFix);
  }
}
