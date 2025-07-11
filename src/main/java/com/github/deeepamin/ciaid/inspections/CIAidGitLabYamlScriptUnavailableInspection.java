package com.github.deeepamin.ciaid.inspections;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.highlighter.quickfix.CreateScriptQuickFix;
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
import org.jetbrains.yaml.psi.YAMLKeyValue;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;

public class CIAidGitLabYamlScriptUnavailableInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (CIAidProjectService.hasGitlabYamlFile(element) &&
                (YamlUtils.isYamlTextElement(element) || YamlUtils.isYamlScalarListOrYamlScalarTextElement(element))) {
          var isChildOfScriptElements = PsiUtils.isChild(element, SCRIPT_KEYWORDS);
          if (isChildOfScriptElements) {
            var filePath = CIAidUtils.handleQuotedText(element.getText());
            var scriptPathIndexes = FileUtils.getFilePathAndIndexes(filePath);
            for (var scriptPathIndex : scriptPathIndexes) {
              var project = element.getProject();
              var scriptPath = scriptPathIndex.path();
              var virtualScriptFile = FileUtils.findVirtualFile(scriptPath, project).orElse(null);
              var isNotScriptBlock = YamlUtils.isYamlTextElement(element) && element.getParent() instanceof YAMLKeyValue;
              if (virtualScriptFile == null) {
                if (isNotScriptBlock) {
                  var errorText = CIAidBundle.message("inspections.gitlab.ci.script-not-found", element.getText());
                  var quickFix = new CreateScriptQuickFix();
                  holder.registerProblem(element, errorText, quickFix);
                }
              }
            }
          }
        }
      }
    };
  }
}
