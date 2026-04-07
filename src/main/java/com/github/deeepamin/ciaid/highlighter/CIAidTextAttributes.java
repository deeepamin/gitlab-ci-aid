package com.github.deeepamin.ciaid.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_METHOD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER;

/**
 * Shared TextAttributesKey constants used by the annotator and the color settings page.
 */
public final class CIAidTextAttributes {
  private CIAidTextAttributes() {
  }

  public static final TextAttributesKey STAGE =
          TextAttributesKey.createTextAttributesKey("CI_AID_GITLAB_STAGE", INSTANCE_FIELD);

  public static final TextAttributesKey JOB =
          TextAttributesKey.createTextAttributesKey("CI_AID_GITLAB_JOB", INSTANCE_METHOD);

  public static final TextAttributesKey SCRIPT_PATH =
          TextAttributesKey.createTextAttributesKey("CI_AID_GITLAB_SCRIPT_PATH", NUMBER);

  public static final TextAttributesKey INCLUDE =
          TextAttributesKey.createTextAttributesKey("CI_AID_GITLAB_INCLUDE", NUMBER);

  public static final TextAttributesKey INPUTS =
          TextAttributesKey.createTextAttributesKey("CI_AID_GITLAB_INPUTS", INSTANCE_FIELD);
}

