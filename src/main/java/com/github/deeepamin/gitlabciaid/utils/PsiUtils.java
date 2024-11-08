package com.github.deeepamin.gitlabciaid.utils;

import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.SCRIPT_KEYWORDS;

public class PsiUtils {
  public static boolean isScriptElement(PsiElement element) {
    return isChild(element, SCRIPT_KEYWORDS);
  }

  public static boolean isIncludeLocalFileElement(PsiElement element) {
    return isChild(element, List.of(INCLUDE));
  }

  public static boolean isChild(PsiElement element, List<String> parentKeys) {
    if (element == null || parentKeys.isEmpty()) {
      return false;
    }
    Optional<YAMLKeyValue> parent = getParent(element, parentKeys);
    return parent.isPresent();
  }

  public static Optional<YAMLKeyValue> getParent(PsiElement element, List<String> parentKeys) {
    if (element == null || parentKeys.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(element)
            .flatMap(PsiUtils::toYAMLKeyValue)
            .filter(keyValue -> parentKeys.stream().anyMatch(keyValue.getKeyText()::equals))
            .or(() -> Optional.of(element)
                    .map(PsiElement::getParent)
                    .flatMap(parent -> getParent(parent, parentKeys))
            );
  }

  public static Optional<YAMLKeyValue> toYAMLKeyValue(PsiElement element) {
    if (element instanceof YAMLKeyValue yamlKeyValue) {
      return Optional.of(yamlKeyValue);
    }
    return Optional.empty();
  }
}
