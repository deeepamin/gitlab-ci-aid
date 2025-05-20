package com.github.deeepamin.ciaid.utils;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

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
}
