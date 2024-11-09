package com.github.deeepamin.gitlabciaid.services.annotators;

import com.github.deeepamin.gitlabciaid.GitlabCIAidBundle;
import com.github.deeepamin.gitlabciaid.services.GitlabCIYamlCache;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.NEEDS;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;

public class GitlabCIYamlAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof YAMLPlainTextImpl || element instanceof YAMLQuotedText)) {
      return;
    }
    annotateNeeds(element, holder);
    annotateStage(element, holder);
  }

  private void annotateStage(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(STAGE)))
            .ifPresent(stage -> {
              var allStages = GitlabCIYamlCache.getStageNamesDefinedAtStagesLevel();
              if (!allStages.contains(psiElement.getText())) {
                holder.newAnnotation(HighlightSeverity.ERROR, GitlabCIAidBundle.message("annotator.gitlabciaid.error.stage-undefined", psiElement.getText()))
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
                holder.newAnnotation(HighlightSeverity.ERROR, GitlabCIAidBundle.message("annotator.gitlabciaid.error.need-job-undefined", psiElement.getText()))
                        .highlightType(LIKE_UNKNOWN_SYMBOL)
                        .create();
              }
            });
  }
}
