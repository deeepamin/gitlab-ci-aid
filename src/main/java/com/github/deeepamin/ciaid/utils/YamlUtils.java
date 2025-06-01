package com.github.deeepamin.ciaid.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YAMLScalarList;
import org.jetbrains.yaml.psi.YAMLScalarText;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Collection;

public class YamlUtils {
  public static @NotNull Collection<YAMLKeyValue> getTopLevelKeysForAllDocuments(YAMLFile file) {
    return file.getDocuments().stream()
            .map(YAMLDocument::getTopLevelValue)
            .filter(YAMLMapping.class::isInstance)
            .map(YAMLMapping.class::cast)
            .map(YAMLMapping::getKeyValues)
            .flatMap(Collection::stream)
            .toList();
  }

  public static boolean isYamlTextElement(PsiElement element) {
    return element instanceof YAMLPlainTextImpl || element instanceof YAMLQuotedText;
  }

  public static boolean isYamlScalarListOrYamlScalarTextElement(PsiElement element) {
    return element instanceof YAMLScalarText || element instanceof YAMLScalarList;
  }

  public static boolean isYamlFile(VirtualFile file) {
    return file != null && file.isValid() && !file.isDirectory() && hasYamlExtension(file.getPath());
  }

  public static boolean hasYamlExtension(String filePath) {
    return filePath != null && (filePath.endsWith(".yml") || filePath.endsWith(".yaml"));
  }
}
