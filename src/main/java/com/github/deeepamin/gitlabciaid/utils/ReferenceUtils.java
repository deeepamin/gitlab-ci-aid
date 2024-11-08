package com.github.deeepamin.gitlabciaid.utils;

import com.github.deeepamin.gitlabciaid.services.resolvers.IncludeFileReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.resolvers.NeedsReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.resolvers.ScriptReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.resolvers.StageReferenceResolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Map;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.services.GitlabCIYamlCache.PLUGIN_DATA;

public class ReferenceUtils {
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
    if (element instanceof YAMLPlainTextImpl) {
      var need = element.getText();
      var targetJob = PLUGIN_DATA.values().stream()
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
      var targetStages = PLUGIN_DATA.values().stream()
              .flatMap(yamlData -> yamlData.getStages().entrySet().stream())
              .filter(entry -> entry.getKey().equals(stageName))
              .map(Map.Entry::getValue)
              .findFirst()
              .orElse(null);
      return Optional.of(new PsiReference[]{ new StageReferenceResolver(element, targetStages) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }
}
