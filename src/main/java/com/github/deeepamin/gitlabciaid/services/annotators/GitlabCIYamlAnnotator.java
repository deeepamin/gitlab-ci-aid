package com.github.deeepamin.gitlabciaid.services.annotators;

import com.github.deeepamin.gitlabciaid.GitlabCIAidBundle;
import com.github.deeepamin.gitlabciaid.services.GitlabCIYamlCache;
import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.NEEDS;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.SCRIPT_KEYWORDS;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.gitlabciaid.utils.FileUtils.SCRIPT_EXTENSIONS;
import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;

public class GitlabCIYamlAnnotator implements Annotator {
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlAnnotator.class);
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof YAMLPlainTextImpl || element instanceof YAMLQuotedText)) {
      return;
    }
    annotateNeeds(element, holder);
    annotateStage(element, holder);
    annotateScriptNotFound(element, holder);
  }

  private void annotateScriptNotFound(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, SCRIPT_KEYWORDS))
            .ifPresent(scriptElement -> {
              var filePath = scriptElement.getText();
              boolean isScript = SCRIPT_EXTENSIONS.stream()
                      .anyMatch(filePath::endsWith);
              if (!isScript) {
                LOG.debug("File extension is not of a script " + scriptElement.getText());
                return;
              }
              var project = scriptElement.getProject();
              var virtualScriptFile = FileUtils.getVirtualFile(filePath, project).orElse(null);
              if (virtualScriptFile == null) {
                var errorText = GitlabCIAidBundle.message("annotator.gitlabciaid.error.script-not-found", scriptElement.getText());
                var quickFix = new CreateScriptQuickFix();
                var problemDescriptor = InspectionManager.getInstance(project)
                                .createProblemDescriptor(scriptElement, errorText, quickFix, LIKE_UNKNOWN_SYMBOL, true);
                holder.newAnnotation(HighlightSeverity.ERROR, errorText)
                        .highlightType(LIKE_UNKNOWN_SYMBOL)
                        .newLocalQuickFix(quickFix, problemDescriptor)
                        .registerFix()
                        .create();
              }
            });
  }

  private void annotateStage(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(STAGE)))
            .ifPresent(stage -> {
              var allStages = GitlabCIYamlCache.getStageNamesDefinedAtStagesLevel();
              if (!allStages.contains(psiElement.getText())) {
                holder.newAnnotation(HighlightSeverity.ERROR, GitlabCIAidBundle.message("annotator.gitlabciaid.error.stage-undefined", stage.getText()))
                        .highlightType(LIKE_UNKNOWN_SYMBOL)
                        .create();
              }
            });
  }

  private void annotateNeeds(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(NEEDS)))
            .ifPresent(job -> {
              var allJobs = GitlabCIYamlCache.getJobNames();
              if (!allJobs.contains(psiElement.getText())) {
                holder.newAnnotation(HighlightSeverity.ERROR, GitlabCIAidBundle.message("annotator.gitlabciaid.error.need-job-undefined", job.getText()))
                        .highlightType(LIKE_UNKNOWN_SYMBOL)
                        .create();
              }
            });
  }
}
