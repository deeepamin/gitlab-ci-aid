package com.github.deeepamin.gitlabciaid.services.annotators;

import com.github.deeepamin.gitlabciaid.GitlabCIAidBundle;
import com.github.deeepamin.gitlabciaid.services.GitlabCIYamlProjectService;
import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.github.deeepamin.gitlabciaid.utils.ReferenceUtils;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.DEFAULT_STAGES;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.NEEDS;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.NEEDS_POSSIBLE_CHILD_KEYWORDS;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.SCRIPT_KEYWORDS;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGES;
import static com.github.deeepamin.gitlabciaid.utils.FileUtils.SCRIPT_EXTENSIONS;
import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD;

public class GitlabCIYamlAnnotator implements Annotator {
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlAnnotator.class);
  private static final String STAGE_ATTRIBUTE = "STAGE";
  private static final String JOB_ATTRIBUTE = "JOB";
  private static final TextAttributesKey STAGE_HIGHLIGHTER = TextAttributesKey.createTextAttributesKey(STAGE_ATTRIBUTE, INSTANCE_FIELD);
  private static final TextAttributesKey JOB_HIGHLIGHTER = TextAttributesKey.createTextAttributesKey(JOB_ATTRIBUTE, INSTANCE_METHOD);

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (PsiUtils.isYamlTextElement(element)) {
      annotateStage(element, holder);
      annotateStages(element, holder);
      annotateNeedsJob(element, holder);
      annotateScriptNotFound(element, holder);
    }
    if (element instanceof LeafPsiElement) {
      annotateJobs(element, holder);
    }
  }

  private void annotateStage(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(STAGE)))
            .ifPresent(stage -> {
              var allStages = GitlabCIYamlProjectService.getStageNamesDefinedAtStagesLevel();
              var stageName = ReferenceUtils.handleQuotedText(psiElement.getText());
              if (!allStages.contains(stageName) && !DEFAULT_STAGES.contains(stageName)) {
                holder.newAnnotation(HighlightSeverity.WARNING, GitlabCIAidBundle.message("annotator.gitlabciaid.error.stage-undefined", stage.getText()))
                        .highlightType(LIKE_UNKNOWN_SYMBOL)
                        .create();
              } else {
                // highlight correct stage
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .textAttributes(STAGE_HIGHLIGHTER)
                        .create();
              }
            });
  }

  private void annotateJobs(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> GitlabCIYamlProjectService.getJobNames().contains(element.getText()))
            .filter(element -> !PsiUtils.isChild(element, List.of(STAGE, STAGES)))  // if stage and job name same, filter stages
            .ifPresent(job -> holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                  .textAttributes(JOB_HIGHLIGHTER)
                  .create()
            );
  }

  private void annotateStages(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(STAGES)))
            .ifPresent(stage -> holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                    .textAttributes(STAGE_HIGHLIGHTER)
                    .create());
  }

  private void annotateNeedsJob(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(NEEDS)))
            .filter(element -> !PsiUtils.isChild(element, NEEDS_POSSIBLE_CHILD_KEYWORDS))
            .ifPresent(job -> {
              var allJobs = GitlabCIYamlProjectService.getJobNames();
              var jobName = ReferenceUtils.handleQuotedText(psiElement.getText());
              if (!allJobs.contains(jobName)) {
                holder.newAnnotation(HighlightSeverity.WARNING, GitlabCIAidBundle.message("annotator.gitlabciaid.error.need-job-undefined", job.getText()))
                        .highlightType(LIKE_UNKNOWN_SYMBOL)
                        .create();
              } else {
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .textAttributes(JOB_HIGHLIGHTER)
                        .create();
              }
            });
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
}
