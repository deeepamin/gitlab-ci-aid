package com.github.deeepamin.gitlabciaid.utils;

import com.github.deeepamin.gitlabciaid.model.GitlabCIYamlData;
import com.github.deeepamin.gitlabciaid.services.resolvers.IncludeFileReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.resolvers.NeedsReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.resolvers.ScriptReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.resolvers.StageReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.resolvers.StagesReferenceResolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.services.GitlabCIYamlApplicationService.getPluginData;

public class ReferenceUtils {
  public static Optional<PsiReference[]> getReferences(PsiElement psiElement) {
    if (PsiUtils.isScriptElement(psiElement)) {
      return referencesScripts(psiElement);
    } else if (PsiUtils.isIncludeLocalFileElement(psiElement)) {
      return referencesIncludeLocalFiles(psiElement);
    } else if (PsiUtils.isNeedsElement(psiElement)) {
      return referencesNeeds(psiElement);
    } else if (PsiUtils.isStagesElement(psiElement)) {
      return referencesStages(psiElement);
    } else if (PsiUtils.isStageElement(psiElement)) {
      return referencesStage(psiElement);
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static Optional<PsiReference[]> referencesScripts(PsiElement element) {
    if (element instanceof YAMLPlainTextImpl) {
      return Optional.of(new PsiReference[]{new ScriptReferenceResolver(element)});
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static Optional<PsiReference[]> referencesIncludeLocalFiles(PsiElement element) {
    if (element instanceof YAMLPlainTextImpl) {
      return Optional.of(new PsiReference[]{new IncludeFileReferenceResolver(element)});
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static Optional<PsiReference[]> referencesNeeds(PsiElement element) {
    if (PsiUtils.isYamlTextElement(element)) {
      // for cases: needs: ["some_job"]
      var need = element.getText().replaceAll("\"", "");
      var targetJob = getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getJobs().entrySet().stream())
              .filter(entry -> entry.getKey().equals(need))
              .map(Map.Entry::getValue)
              .findFirst()
              .orElse(null);
      return Optional.of(new PsiReference[]{new NeedsReferenceResolver(element, targetJob)});
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static Optional<PsiReference[]> referencesStages(PsiElement element) {
    if (element instanceof YAMLPlainTextImpl) {
      var stageName = element.getText();
      var targetStages = getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getStages().entrySet().stream())
              .filter(entry -> entry.getKey().equals(stageName))
              .map(Map.Entry::getValue)
              .flatMap(List::stream)
              .toList();
      return Optional.of(new PsiReference[]{ new StageReferenceResolver(element, targetStages) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static Optional<PsiReference[]> referencesStage(PsiElement element) {
    if (element instanceof YAMLPlainTextImpl) {
      var stageName = element.getText();
      var parent = getPluginData().values()
              .stream()
              .map(GitlabCIYamlData::getStagesElement)
              .filter(Objects::nonNull)
              .findFirst().orElse(null);
      var children = PsiUtils.findChildren(parent, YAMLPlainTextImpl.class);
      var targetChild = children.stream()
              .filter(child -> child.getText().equals(stageName))
              .findFirst()
              .orElse(null);
      if (targetChild != null) {
        return Optional.of(new PsiReference[]{new StagesReferenceResolver(element, targetChild)});
      }
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }
}
