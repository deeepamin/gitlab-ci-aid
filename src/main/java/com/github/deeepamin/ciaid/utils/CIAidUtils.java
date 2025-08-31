package com.github.deeepamin.ciaid.utils;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

import java.net.URI;
import java.net.URL;

public class CIAidUtils {
  public static TextRange getHighlightTextRange(PsiElement element, int start, int end) {
    var elementTextRange = element.getTextRange();
    var highlightStartRange = elementTextRange.getStartOffset() + start;
    if (highlightStartRange > elementTextRange.getEndOffset()) {
      highlightStartRange = elementTextRange.getStartOffset();
    }
    var highlightEndRange = elementTextRange.getStartOffset() + end;
    if (highlightEndRange > elementTextRange.getEndOffset()) {
      highlightEndRange = elementTextRange.getEndOffset();
    }
    return new TextRange(highlightStartRange, highlightEndRange);
  }

  public static boolean isValidUrl(String input) {
    if (input == null) {
      return false;
    }
    if (input.isBlank()) {
      // allow empty URLs for default value
      return true;
    }
    try {
      URL url = URI.create(input).toURL();
      return url.getProtocol().equals("http") || url.getProtocol().equals("https");
    } catch (Exception e) {
      return false;
    }
  }

  public static String handleQuotedText(String text) {
    if (text.startsWith("\"") && text.endsWith("\"")) {
      text = text.replaceAll("\"", "");
    } else if (text.startsWith("'") && text.endsWith("'")) {
      text = text.replaceAll("'", "");
    }
    return text;
  }

  public static boolean containsWildcardWithYmlExtension(String path) {
    return containsWildcard(path) && (path.endsWith(".yml") || path.endsWith(".yaml"));
  }

  public static boolean containsWildcard(String path) {
    return path != null && path.contains("*");
  }
}
