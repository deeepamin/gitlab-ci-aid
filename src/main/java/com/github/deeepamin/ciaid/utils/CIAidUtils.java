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
    if (input == null || input.isBlank()) {
      return false;
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
}
