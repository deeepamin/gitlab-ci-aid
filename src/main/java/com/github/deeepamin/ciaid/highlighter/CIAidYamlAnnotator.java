package com.github.deeepamin.ciaid.highlighter;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.references.providers.IncludeReferenceProvider;
import com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.services.CIAidProjectService.getCIAidProjectService;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER;

public class CIAidYamlAnnotator implements Annotator {
  private static final Logger LOG = Logger.getInstance(CIAidYamlAnnotator.class);
  private static final String STAGE_ATTRIBUTE = "STAGE";
  private static final String JOB_ATTRIBUTE = "JOB";
  private static final String SCRIPT_ATTRIBUTE = "SCRIPT";
  private static final String INCLUDE_ATTRIBUTE = "INCLUDE";

  private static final TextAttributesKey STAGE_HIGHLIGHTER = TextAttributesKey.createTextAttributesKey(STAGE_ATTRIBUTE, INSTANCE_FIELD);
  private static final TextAttributesKey JOB_HIGHLIGHTER = TextAttributesKey.createTextAttributesKey(JOB_ATTRIBUTE, INSTANCE_METHOD);
  private static final TextAttributesKey SCRIPT_HIGHLIGHTER = TextAttributesKey.createTextAttributesKey(SCRIPT_ATTRIBUTE, NUMBER);
  private static final TextAttributesKey INCLUDE_HIGHLIGHTER = TextAttributesKey.createTextAttributesKey(INCLUDE_ATTRIBUTE, NUMBER);

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!CIAidProjectService.hasGitlabYamlFile(element)) {
      LOG.debug(String.format("%s is not an element in Gitlab CI Yaml.", element.getText()));
      return;
    }
    if (element instanceof LeafPsiElement) {
      highlightJobs(element, holder);
    } else if (YamlUtils.isYamlTextElement(element)) {
      highlightNeedsJob(element, holder);
      highlightStage(element, holder);
      highlightStages(element, holder);
      highlightIncludeFile(element, holder);
      highlightScript(element, holder);
      highlightInputs(element, holder);
    } else if (YamlUtils.isYamlScalarListOrYamlScalarTextElement(element)) {
      highlightScript(element, holder);
    }
  }

  private void highlightInputs(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    var elementText = element.getText();
    var inputsWithStartEndRange = InputsReferenceProvider.getInputs(elementText);
    if (inputsWithStartEndRange == null || inputsWithStartEndRange.isEmpty()) {
      return;
    }

    inputsWithStartEndRange.forEach(input -> {
      if (input.path() != null) {
        var highlightRange = CIAidUtils.getHighlightTextRange(element, input.start(), input.end());
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .textAttributes(STAGE_HIGHLIGHTER)
                .range(highlightRange)
                .create();
      }
    });
  }

  private void highlightStage(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(GitlabCIYamlUtils::isJobStageElement)
            .ifPresent(stage -> {
              var allStages = getCIAidProjectService(psiElement)
                      .getDataProvider()
                      .getStageNamesDefinedAtStagesLevel();
              var stageName = CIAidUtils.handleQuotedText(psiElement.getText());
              var isInputsString = InputsReferenceProvider.isAnInputsString(stageName);
              if (isInputsString) {
                return;
              }
              boolean isStageInJobElement = false;
              var elementParent = stage.getParent();
              if (elementParent instanceof YAMLKeyValue keyValue) {
                isStageInJobElement = keyValue.getKeyText().equals(STAGE);
              }
              if (!isStageInJobElement) {
                return;
              }
              if (allStages.contains(stageName) || DEFAULT_STAGES.contains(stageName)) {
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .textAttributes(STAGE_HIGHLIGHTER)
                        .create();
              }
            });
  }

  private void highlightJobs(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> getCIAidProjectService(psiElement)
                    .getDataProvider()
                    .getJobNames()
                    .contains(element.getText()))
            .filter(element -> !PsiUtils.isChild(element, List.of(STAGE, STAGES)))  // if stage and job name same, filter stages
            .ifPresent(job -> holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                  .textAttributes(JOB_HIGHLIGHTER)
                  .create()
            );
  }

  private void highlightStages(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(GitlabCIYamlUtils::isStagesElement)
            .ifPresent(stage -> holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                    .textAttributes(STAGE_HIGHLIGHTER)
                    .create());
  }

  private void highlightNeedsJob(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(GitlabCIYamlUtils::isNeedsElement)
            .ifPresent(job -> {
              var allJobs = getCIAidProjectService(psiElement)
                      .getDataProvider()
                      .getJobNames();
              var jobName = CIAidUtils.handleQuotedText(psiElement.getText());
              var isInputsString = InputsReferenceProvider.isAnInputsString(jobName);
              if (isInputsString) {
                return;
              }
              if (allJobs.contains(jobName)) {
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .textAttributes(JOB_HIGHLIGHTER)
                        .create();
              }
            });
  }

  private void highlightScript(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(GitlabCIYamlUtils::isScriptElement)
            .ifPresent(scriptElement -> {
              var filePath = CIAidUtils.handleQuotedText(scriptElement.getText());
              var scriptPathIndexes = FileUtils.getFilePathAndIndexes(filePath);
              for (var scriptPathIndex : scriptPathIndexes) {
                var project = scriptElement.getProject();
                var scriptPath = scriptPathIndex.path();
                var virtualScriptFile = FileUtils.findVirtualFile(scriptPath, project).orElse(null);
                if (virtualScriptFile != null) {
                  var highlightRange = CIAidUtils.getHighlightTextRange(scriptElement, scriptPathIndex.start(), scriptPathIndex.end());
                  holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                          .textAttributes(SCRIPT_HIGHLIGHTER)
                          .range(highlightRange)
                          .create();
                }
              }
            });
  }

  private void highlightIncludeFile(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(GitlabCIYamlUtils::isIncludeElement)
            .ifPresent(includeElement -> {
              var isNonLocalInclude = PsiUtils.isChild(includeElement, NON_LOCAL_INCLUDE_KEYWORDS);
              if (isNonLocalInclude) {
                var cacheKey = IncludeReferenceProvider.getIncludeCacheKey(includeElement);
                if (cacheKey != null) {
                  var path = CIAidCacheService.getInstance().getIncludeCacheFilePathFromKey(cacheKey);
                  if (path != null) {
                    holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                            .textAttributes(INCLUDE_HIGHLIGHTER)
                            .create();
                  }
                }
                return;
              }
              var filePath = CIAidUtils.handleQuotedText(includeElement.getText());
              var inputsFilePathString = InputsReferenceProvider.isAnInputsString(filePath);
              if (inputsFilePathString) {
                return;
              }
              var project = includeElement.getProject();
              var pathContainsWildcard = CIAidUtils.containsWildcardWithYmlExtension(filePath);
              if (pathContainsWildcard) {
                var includeFiles = FileUtils.findVirtualFilesByGlob(filePath, project);
                if (includeFiles.isEmpty()) {
                  return;
                } else {
                  holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                          .textAttributes(INCLUDE_HIGHLIGHTER)
                          .create();
                }
              }
              var includeVirtualFile = FileUtils.findVirtualFile(filePath, project).orElse(null);
              var isRemoteInclude = CIAidUtils.isValidUrl(filePath);
              var isTriggerInclude = PsiUtils.isChild(includeElement, List.of(TRIGGER));
              if (includeVirtualFile != null && !isRemoteInclude && !isTriggerInclude) {
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .textAttributes(INCLUDE_HIGHLIGHTER)
                        .create();
              }
            });
  }

}
