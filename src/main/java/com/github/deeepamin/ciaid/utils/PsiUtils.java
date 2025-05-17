package com.github.deeepamin.ciaid.utils;

import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.EXTENDS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.INPUTS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.NEEDS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.SCRIPT_KEYWORDS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.SPEC;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.STAGES;

public class PsiUtils {
  public static boolean isScriptElement(PsiElement element) {
    return isChild(element, SCRIPT_KEYWORDS);
  }

  public static boolean isIncludeLocalFileElement(PsiElement element) {
    return isChild(element, List.of(INCLUDE));
  }

  public static boolean isNeedsElement(PsiElement element) {
    return isChild(element, List.of(NEEDS)) && isNotSpecInputsElement(element);
  }

  public static boolean isStagesElement(PsiElement element) {
    return isChild(element, List.of(STAGES)) && isNotSpecInputsElement(element);
  }

  public static boolean isStageElement(PsiElement element) {
    return isChild(element, List.of(STAGE)) && isNotSpecInputsElement(element);
  }

  public static boolean isExtendsElement(PsiElement element) {
    return isChild(element, List.of(EXTENDS));
  }

  public static boolean isNotSpecInputsElement(PsiElement element) {
    return !isChild(element, List.of(SPEC, INPUTS));
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

  @SuppressWarnings("unchecked")
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

  public static boolean hasChild(PsiElement element, String childKey) {
    if (element == null || childKey == null) {
      return false;
    }
    if (element instanceof YAMLKeyValue keyValue) {
      if (keyValue.getKeyText().equals(childKey)) {
        return true;
      }
    }
    if (element.getText().equals(childKey)) {
      return true;
    }
    var children = element.getChildren();
    for (PsiElement child : children) {
      if (hasChild(child, childKey)) {
        return true;
      }
    }
    return false;
  }

  public static PsiElement findChildWithKey(final PsiElement element, final String childKey) {
    if (element == null) {
      return null;
    }
    for (PsiElement child : element.getChildren()) {
      if (child instanceof YAMLKeyValue keyValue) {
        if (childKey.equals(keyValue.getKeyText())) {
          return child;
        }
      } else if (child instanceof YAMLScalar yamlScalar) {
        if (childKey.equals(yamlScalar.getTextValue())) {
          return child;
        }
      }
      var found = findChildWithKey(child, childKey);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  public static <T extends PsiElement>  Optional<T> findParentOfType(PsiElement element,  final Class<T> clazz) {
    if (element == null) {
      return Optional.empty();
    }
    if (clazz.isInstance(element)) {
      return Optional.of(clazz.cast(element));
    }
    return findParentOfType(element.getParent(), clazz);
  }
}
