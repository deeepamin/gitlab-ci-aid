package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.model.gitlab.Input;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.services.resolvers.IncludeFileReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.InputsReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.JobStageToStagesReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.NeedsOrExtendsToJobReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.ScriptReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.StagesToJobStageReferenceResolver;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReferenceUtils {
  public static Optional<PsiReference[]> getReferences(PsiElement psiElement) {
    if (PsiUtils.isScriptElement(psiElement)) {
      return referencesScripts(psiElement);
    } else if (PsiUtils.isIncludeLocalFileElement(psiElement)) {
      return referencesIncludeLocalFiles(psiElement);
    } else if (PsiUtils.isNeedsElement(psiElement) || PsiUtils.isExtendsElement(psiElement)) {
      return referencesNeedsOrExtendsToJob(psiElement);
    } else if (PsiUtils.isStagesElement(psiElement)) {
      return referencesStagesToStage(psiElement);
    } else if (PsiUtils.isStageElement(psiElement)) {
      return referencesStageToStages(psiElement);
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static @NotNull Optional<PsiReference[]> getInputReferences(@NotNull PsiElement psiElement) {
      return referencesInputToInputs(psiElement);
  }

  private static Optional<PsiReference[]> referencesScripts(PsiElement element) {
    if (YamlUtils.isYamlTextElement(element) || YamlUtils.isYamlScalarListOrYamlScalarTextElement(element)) {
      var scriptText = handleQuotedText(element.getText());
      var scriptPathIndexes = FileUtils.getFilePathAndIndexes(scriptText);
      if (scriptPathIndexes.isEmpty()) {
        return Optional.of(PsiReference.EMPTY_ARRAY);
      }
      List<PsiReference> references = new ArrayList<>();
      for (var scriptPathIndex : scriptPathIndexes) {
        references.add(new ScriptReferenceResolver(element, new TextRange(scriptPathIndex.start(), scriptPathIndex.end())));
      }
      return Optional.of(references.toArray(new PsiReference[0]));
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  private static Optional<PsiReference[]> referencesIncludeLocalFiles(PsiElement element) {
    if (YamlUtils.isYamlTextElement(element)) {
      return Optional.of(new PsiReference[]{ new IncludeFileReferenceResolver(element) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  private static Optional<PsiReference[]> referencesNeedsOrExtendsToJob(PsiElement element) {
    if (YamlUtils.isYamlTextElement(element)) {
      // for cases: needs: ["some_job"] / needs: ["some_job] / needs: [some_job"]
      var need = handleQuotedText(element.getText());
      var project = element.getProject();
      var gitlabCIYamlProjectService = CIAidProjectService.getInstance(project);
      var targetJob = gitlabCIYamlProjectService.getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getJobNameToJobElement().entrySet().stream())
              .filter(entry -> entry.getKey().equals(need))
              .map(Map.Entry::getValue)
              .filter(pointer -> pointer.getElement() != null)
              .map(SmartPsiElementPointer::getElement)
              .findFirst()
              .orElse(null);
      return Optional.of(new PsiReference[]{ new NeedsOrExtendsToJobReferenceResolver(element, targetJob) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  private static Optional<PsiReference[]> referencesStagesToStage(PsiElement element) {
    if (YamlUtils.isYamlTextElement(element)) {
      var stageName = handleQuotedText(element.getText());
      var project = element.getProject();
      var gitlabCIYamlProjectService = CIAidProjectService.getInstance(project);
      var targetStages = gitlabCIYamlProjectService.getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getStageNameToStageElements().entrySet().stream())
              .filter(entry -> entry.getKey().equals(stageName))
              .map(Map.Entry::getValue)
              .flatMap(List::stream)
              .filter(pointer -> pointer.getElement() != null)
              .map(SmartPsiElementPointer::getElement)
              .toList();
      return Optional.of(new PsiReference[]{ new StagesToJobStageReferenceResolver(element, targetStages) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  private static Optional<PsiReference[]> referencesStageToStages(PsiElement element) {
    if (YamlUtils.isYamlTextElement(element)) {
      var stageName = handleQuotedText(element.getText());
      var project = element.getProject();
      var gitlabCIYamlProjectService = CIAidProjectService.getInstance(project);

      var target = gitlabCIYamlProjectService.getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getStagesItemNameToStagesElement().entrySet().stream())
              .filter(entry -> entry.getKey().equals(stageName))
              .map(Map.Entry::getValue)
              .filter(pointer -> pointer.getElement() != null)
              .map(SmartPsiElementPointer::getElement)
              .findFirst()
              .orElse(null);
      if (target != null) {
        return Optional.of(new PsiReference[]{ new JobStageToStagesReferenceResolver(element, target) });
      }
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  private static Optional<PsiReference[]> referencesInputToInputs(PsiElement element) {
    if (element instanceof YAMLPsiElement) {
      var inputString = element.getText();
      var inputNameWithStartEndRange = GitlabCIYamlUtils.getInputNameFromInputsString(inputString);
      if (inputNameWithStartEndRange == null) {
        return Optional.of(PsiReference.EMPTY_ARRAY);
      }

      var inputName = inputNameWithStartEndRange.path();
      var startOffset = inputNameWithStartEndRange.start();
      var endOffset = inputNameWithStartEndRange.end();
      var project = element.getProject();
      var gitlabCIYamlProjectService = CIAidProjectService.getInstance(project);
      var targetInput = gitlabCIYamlProjectService.getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getInputs().stream())
              .filter(input -> input.name().equals(inputName))
              .map(Input::inputElement)
              .filter(pointer -> pointer.getElement() != null)
              .map(SmartPsiElementPointer::getElement)
              .findFirst()
              .orElse(null);
      return Optional.of(new PsiReference[]{ new InputsReferenceResolver(element, targetInput, TextRange.create(startOffset, endOffset)) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static String handleQuotedText(String text) {
    if (text.startsWith("\"") && text.endsWith("\"")) {
      text = text.replaceAll("\"", "");
    } else if (text.startsWith("'") && text.endsWith("'")) {
      text = text.replaceAll("'", "");
    }
    return text;
  }

}
