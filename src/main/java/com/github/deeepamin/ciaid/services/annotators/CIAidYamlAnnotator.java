package com.github.deeepamin.ciaid.services.annotators;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.ReferenceUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.DEFAULT_STAGES;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.INCLUDE_POSSIBLE_CHILD_KEYWORDS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.NEEDS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.NEEDS_POSSIBLE_CHILD_KEYWORDS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.SCRIPT_KEYWORDS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.STAGES;
import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.getGitlabCIYamlProjectService;
import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;
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
    if (!GitlabCIYamlUtils.hasGitlabYamlFile(element)) {
      LOG.debug(String.format("%s is not an element in Gitlab CI Yaml.", element.getText()));
      return;
    }
    if (element instanceof LeafPsiElement) {
      highlightJobs(element, holder);
    } else if (YamlUtils.isYamlTextElement(element)) {
      annotateHighlightNeedsJob(element, holder);
      annotateHighlightStage(element, holder);
      annotateStages(element, holder);
      annotateHighlightIncludeFile(element, holder);
      annotateHighlightScript(element, holder);
      highlightInputs(element, holder);
    } else if (YamlUtils.isYamlScalarListOrYamlScalarTextElement(element)) {
      annotateHighlightScript(element, holder);
    }
  }

  private void highlightInputs(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    var elementText = element.getText();
    var inputsStringWithStartEndRange = GitlabCIYamlUtils.getInputsString(elementText);
    if (inputsStringWithStartEndRange == null || inputsStringWithStartEndRange.path() == null) {
      return;
    }

    var highlightRange = getHighlightTextRange(element, inputsStringWithStartEndRange.start(), inputsStringWithStartEndRange.end());
    holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .textAttributes(STAGE_HIGHLIGHTER)
            .range(highlightRange)
            .create();
  }

  private void annotateHighlightStage(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(STAGE)))
            .ifPresent(stage -> {
              var allStages = getGitlabCIYamlProjectService(psiElement).getStageNamesDefinedAtStagesLevel();
              var stageName = ReferenceUtils.handleQuotedText(psiElement.getText());
              var isInputsString = GitlabCIYamlUtils.isAnInputsString(stageName);
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
              if (!allStages.contains(stageName) && !DEFAULT_STAGES.contains(stageName)) {
                var ignoreUndefinedStageErrors = CIAidSettingsState.getInstance(psiElement.getProject()).ignoreUndefinedStage;
                if (!ignoreUndefinedStageErrors) {
                  holder.newAnnotation(HighlightSeverity.WARNING, CIAidBundle.message("annotator.error.stage-undefined", stage.getText()))
                          .highlightType(LIKE_UNKNOWN_SYMBOL)
                          .create();
                }
              } else {
                // highlight correct stage
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .textAttributes(STAGE_HIGHLIGHTER)
                        .create();
              }
            });
  }

  private void highlightJobs(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> getGitlabCIYamlProjectService(psiElement).getJobNames().contains(element.getText()))
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

  private void annotateHighlightNeedsJob(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(NEEDS)))
            .filter(element -> !PsiUtils.isChild(element, NEEDS_POSSIBLE_CHILD_KEYWORDS))
            .ifPresent(job -> {
              var allJobs = getGitlabCIYamlProjectService(psiElement).getJobNames();
              var jobName = ReferenceUtils.handleQuotedText(psiElement.getText());
              var isInputsString = GitlabCIYamlUtils.isAnInputsString(jobName);
              if (isInputsString) {
                return;
              }
              if (!allJobs.contains(jobName)) {
                var ignoreUndefinedJobErrors = CIAidSettingsState.getInstance(psiElement.getProject()).ignoreUndefinedJob;
                if (!ignoreUndefinedJobErrors) {
                  holder.newAnnotation(HighlightSeverity.WARNING, CIAidBundle.message("annotator.error.need-job-undefined", job.getText()))
                          .highlightType(LIKE_UNKNOWN_SYMBOL)
                          .create();
                }
              } else {
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .textAttributes(JOB_HIGHLIGHTER)
                        .create();
              }
            });
  }

  private void annotateHighlightScript(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, SCRIPT_KEYWORDS))
            .ifPresent(scriptElement -> {
              var filePath = ReferenceUtils.handleQuotedText(scriptElement.getText());
              var scriptPathIndexes = FileUtils.getFilePathAndIndexes(filePath);
              for (var scriptPathIndex : scriptPathIndexes) {
                var project = scriptElement.getProject();
                var scriptPath = scriptPathIndex.path();
                var virtualScriptFile = FileUtils.findVirtualFile(scriptPath, project).orElse(null);
                var isNotScriptBlock = YamlUtils.isYamlTextElement(scriptElement) && scriptElement.getParent() instanceof YAMLKeyValue;
                if (virtualScriptFile == null) {
                  // in block any command can be quoted/plain text, and then we don't want to show path related error
                  if (isNotScriptBlock) {
                    var ignoreUndefinedScriptErrors = CIAidSettingsState.getInstance(psiElement.getProject()).ignoreUndefinedScript;
                    if (!ignoreUndefinedScriptErrors) {
                      var errorText = CIAidBundle.message("annotator.error.script-not-found", scriptElement.getText());
                      var quickFix = new CreateScriptQuickFix();
                      var problemDescriptor = InspectionManager.getInstance(project)
                              .createProblemDescriptor(scriptElement, errorText, quickFix, LIKE_UNKNOWN_SYMBOL, true);
                      holder.newAnnotation(HighlightSeverity.ERROR, errorText)
                              .highlightType(LIKE_UNKNOWN_SYMBOL)
                              .newLocalQuickFix(quickFix, problemDescriptor)
                              .registerFix()
                              .create();
                    }
                  }
                } else {
                  var highlightRange = getHighlightTextRange(scriptElement, scriptPathIndex.start(), scriptPathIndex.end());
                  holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                          .textAttributes(SCRIPT_HIGHLIGHTER)
                          .range(highlightRange)
                          .create();
                }
              }
            });
  }

  private void annotateHighlightIncludeFile(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    Optional.of(psiElement)
            .filter(element -> PsiUtils.isChild(element, List.of(INCLUDE)))
            .filter(element -> !PsiUtils.isChild(element, INCLUDE_POSSIBLE_CHILD_KEYWORDS)) // component, project, etc. currently not supported
            .ifPresent(includeElement -> {
              var filePath = ReferenceUtils.handleQuotedText(includeElement.getText());
              var inputsFilePathString = GitlabCIYamlUtils.isAnInputsString(filePath);
              if (inputsFilePathString) {
                return;
              }
              var project = includeElement.getProject();
              var virtualScriptFile = FileUtils.findVirtualFile(filePath, project).orElse(null);
              if (virtualScriptFile == null) {
                var ignoreUndefinedIncludeErrors = CIAidSettingsState.getInstance(psiElement.getProject()).ignoreUndefinedInclude;
                if (!ignoreUndefinedIncludeErrors) {
                  var errorText = CIAidBundle.message("annotator.error.include-not-found", includeElement.getText());
                  var quickFix = new CreateIncludeFileQuickFix();
                  var problemDescriptor = InspectionManager.getInstance(project)
                          .createProblemDescriptor(includeElement, errorText, quickFix, LIKE_UNKNOWN_SYMBOL, true);
                  holder.newAnnotation(HighlightSeverity.WARNING, errorText)
                          .highlightType(LIKE_UNKNOWN_SYMBOL)
                          .newLocalQuickFix(quickFix, problemDescriptor)
                          .registerFix()
                          .create();
                }
              }  else {
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                        .textAttributes(INCLUDE_HIGHLIGHTER)
                        .create();
              }
            });
  }

  private TextRange getHighlightTextRange(PsiElement element, int start, int end) {
    var elementTextRange = element.getTextRange();
    var highlightStartRange = elementTextRange.getStartOffset() + start;
    if (highlightStartRange > elementTextRange.getEndOffset()) {
      highlightStartRange = elementTextRange.getStartOffset();
    }
    var highlightEndRange = elementTextRange.getStartOffset() + end;
    if (highlightEndRange > elementTextRange.getEndOffset()) {
      highlightEndRange = elementTextRange.getEndOffset();
    }
    return new TextRange(highlightStartRange, highlightEndRange);
  }

}
