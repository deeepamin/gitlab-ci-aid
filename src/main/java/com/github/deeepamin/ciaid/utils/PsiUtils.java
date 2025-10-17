package com.github.deeepamin.ciaid.utils;

import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PsiUtils {
  private static final int MAX_RECURSION_DEPTH = 10;

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
    return hasChild(element, childKey, 0, MAX_RECURSION_DEPTH);
  }

  private static boolean hasChild(PsiElement element, String childKey, int depth, int maxDepth) {
    if (element == null || childKey == null || depth > maxDepth) {
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
    for (PsiElement child : element.getChildren()) {
      if (hasChild(child, childKey, depth + 1, maxDepth)) {
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
