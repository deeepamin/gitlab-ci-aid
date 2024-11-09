package com.github.deeepamin.gitlabciaid.utils;

import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.NEEDS;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.SCRIPT_KEYWORDS;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGES;

public class PsiUtils {
  public static boolean isScriptElement(PsiElement element) {
    return isChild(element, SCRIPT_KEYWORDS);
  }

  public static boolean isIncludeLocalFileElement(PsiElement element) {
    return isChild(element, List.of(INCLUDE));
  }

  public static boolean isNeedsElement(PsiElement element) {
    return isChild(element, List.of(NEEDS));
  }

  public static boolean isStagesElement(PsiElement element) {
    return isChild(element, List.of(STAGES));
  }

  public static boolean isStageElement(PsiElement element) {
    return isChild(element, List.of(STAGE));
  }

  public static boolean isChild(PsiElement element, List<String> parentKeys) {
    Optional<YAMLKeyValue> parent = findParent(element, parentKeys);
    return parent.isPresent();
  }

  public static Optional<YAMLKeyValue> findParent(PsiElement element, List<String> parentKeys) {
    if (element == null || parentKeys.isEmpty()) {
      return Optional.empty();
    }
    if (element instanceof YAMLKeyValue keyValue) {
      var keyText = keyValue.getKeyText();
      if (parentKeys.contains(keyText)) {
        return Optional.of(keyValue);
      }
    }
    return findParent(element.getParent(), parentKeys);
  }

  public static <T extends PsiElement> List<T> findChildren(final PsiElement element, final Class<T> clazz) {
    var children = new ArrayList<T>();
    findChildren(element, clazz, children);
    return children;
  }

  private static <T extends PsiElement> void findChildren(final PsiElement element, final Class<T> clazz, List<T> children) {
    if (element == null) {
      return;
    }
    if (clazz.isInstance(element)) {
      children.add((T) element);
    }
    for (PsiElement child : element.getChildren()) {
      findChildren(child, clazz, children);
    }
  }
}
