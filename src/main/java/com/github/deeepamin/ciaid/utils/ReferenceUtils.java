package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.services.resolvers.IncludeFileReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.InputsReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.JobStageToStagesReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.NeedsOrExtendsToJobReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.RefTagReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.ScriptReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.StagesToJobStageReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.VariablesReferenceResolver;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.ArrayList;
import java.util.List;
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
      return referencesStagesToJobStage(psiElement);
    } else if (PsiUtils.isStageElement(psiElement)) {
      return referencesJobStageToStages(psiElement);
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static @NotNull Optional<PsiReference[]> getReferencesToInputOrRefTag(@NotNull PsiElement psiElement) {
      return referencesInputsOrRefTagOrVariables(psiElement);
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
              .flatMap(yamlData -> yamlData.getJobElements().stream())
              .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
              .map(SmartPsiElementPointer::getElement)
              .filter(job -> handleQuotedText(job.getKeyText()).equals(need))
              .findFirst()
              .orElse(null);
      return Optional.of(new PsiReference[]{ new NeedsOrExtendsToJobReferenceResolver(element, targetJob) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  private static Optional<PsiReference[]> referencesStagesToJobStage(PsiElement element) {
    if (YamlUtils.isYamlTextElement(element)) {
      var stageName = handleQuotedText(element.getText());
      var project = element.getProject();
      var gitlabCIYamlProjectService = CIAidProjectService.getInstance(project);

      var targetStages = gitlabCIYamlProjectService.getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getJobStageElements().stream())
              .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
              .map(SmartPsiElementPointer::getElement)
              .filter(stage -> handleQuotedText(stage.getText()).equals(stageName))
              .toList();
      return Optional.of(new PsiReference[]{ new StagesToJobStageReferenceResolver(element, targetStages) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  private static Optional<PsiReference[]> referencesJobStageToStages(PsiElement element) {
    if (YamlUtils.isYamlTextElement(element)) {
      var stageName = handleQuotedText(element.getText());
      var project = element.getProject();
      var gitlabCIYamlProjectService = CIAidProjectService.getInstance(project);

      var target = gitlabCIYamlProjectService.getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getStagesItemElements().stream())
              .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
              .map(SmartPsiElementPointer::getElement)
              .filter(stage -> handleQuotedText(stage.getText()).equals(stageName))
              .findFirst()
              .orElse(null);
      return Optional.of(new PsiReference[]{ new JobStageToStagesReferenceResolver(element, target) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  private static Optional<PsiReference[]> referencesInputsOrRefTagOrVariables(PsiElement element) {
    if (element instanceof YAMLPsiElement yamlPsiElement) {
      var elementText = element.getText();
      var project = element.getProject();
      var gitlabCIYamlProjectService = CIAidProjectService.getInstance(project);
      var inputNamesWithStartEndRange = GitlabCIYamlUtils.getInputNames(elementText);
      if (inputNamesWithStartEndRange != null && !inputNamesWithStartEndRange.isEmpty()) {
        var inputRefs = new ArrayList<PsiReference>();
        inputNamesWithStartEndRange.forEach(inputNameWithStartEndRange -> {
          var inputName = inputNameWithStartEndRange.path();
          var startOffset = inputNameWithStartEndRange.start();
          var endOffset = inputNameWithStartEndRange.end();

          var targetInput = gitlabCIYamlProjectService.getPluginData().values()
                  .stream()
                  .flatMap(yamlData -> yamlData.getInputs().stream())
                  .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
                  .map(SmartPsiElementPointer::getElement)
                  .filter(inputKeyValue -> inputKeyValue.getKeyText().equals(inputName))
                  .findFirst()
                  .orElse(null);

          inputRefs.add(new InputsReferenceResolver(element, targetInput, TextRange.create(startOffset, endOffset)));
        });
        return Optional.of(inputRefs.toArray(PsiReference[]::new));
      }
      var refTagText = GitlabCIYamlUtils.getReferenceTag(yamlPsiElement);
      if (refTagText != null) {
        // reference tag is like !reference [.some_job, key], and .some_job is a top level element so it is inside jobs
        var refersTo = gitlabCIYamlProjectService.getPluginData().values()
                .stream()
                .flatMap(yamlData -> yamlData.getJobElements().stream())
                .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
                .map(SmartPsiElementPointer::getElement)
                .filter(job -> handleQuotedText(job.getKeyText()).equals(refTagText))
                .findFirst()
                .orElse(null);
        // !reference [.some_job, key]: the element could be either .some_job or key
        if (refTagText.equals(elementText)) {
          return Optional.of(new PsiReference[]{ new RefTagReferenceResolver(element, refersTo) });
        } else {
          var keyToReferTo = PsiUtils.findChildWithKey(refersTo, elementText);
          return Optional.of(new PsiReference[]{ new RefTagReferenceResolver(element, keyToReferTo) });
        }
      }
      var variables = GitlabCIYamlUtils.getVariables(elementText);
      if (!variables.isEmpty()) {
        var psiRefs = new ArrayList<PsiReference>();
        variables.forEach(variableWithStartEndRange -> {
          var variableName = variableWithStartEndRange.path();
          var startOffset = variableWithStartEndRange.start();
          var endOffset = variableWithStartEndRange.end();

          var targetVariables = gitlabCIYamlProjectService.getPluginData().values()
                  .stream()
                  .flatMap(yamlData -> yamlData.getVariables().stream())
                  .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
                  .map(SmartPsiElementPointer::getElement)
                  .filter(variableKeyValue -> variableKeyValue.getKeyText().equals(variableName))
                  .toList();
          psiRefs.add(new VariablesReferenceResolver(element, targetVariables, TextRange.create(startOffset, endOffset)));
        });
        return Optional.of(psiRefs.toArray(PsiReference[]::new));
      }
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
