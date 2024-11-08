package com.github.deeepamin.gitlabciaid.utils;

import com.github.deeepamin.gitlabciaid.services.IncludeFileReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.NeedsReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.ScriptReferenceResolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Map;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.services.GitlabCIYamlPostStartup.PATH_TO_YAML_DATA;

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
      var targetJob = PATH_TO_YAML_DATA.values().stream()
              .flatMap(yamlData -> yamlData.getJobs().entrySet().stream())
              .filter(entry -> entry.getKey().equals(need))
              .map(Map.Entry::getValue)
              .findFirst()
              .orElse(null);
      return Optional.of(new PsiReference[]{new NeedsReferenceResolver(element, targetJob)});
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }
}
