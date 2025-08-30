package com.github.deeepamin.ciaid.utils;

import com.intellij.psi.PsiElement;

import java.util.List;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;

public class GitlabCIYamlUtils {

  public static boolean isScriptElement(PsiElement element) {
    return PsiUtils.isChild(element, SCRIPT_KEYWORDS);
  }

  public static boolean isIncludeElement(PsiElement element) {
    return PsiUtils.isChild(element, List.of(INCLUDE)) && !PsiUtils.isChild(element, List.of(TRIGGER));
  }

  public static boolean isNeedsElement(PsiElement element) {
    return PsiUtils.isChild(element, List.of(NEEDS)) && !PsiUtils.isChild(element, NEEDS_POSSIBLE_CHILD_KEYWORDS) && isNotSpecInputsElement(element);
  }

  public static boolean isStagesElement(PsiElement element) {
    return PsiUtils.isChild(element, List.of(STAGES)) && isNotSpecInputsElement(element);
  }

  public static boolean isJobStageElement(PsiElement element) {
    return PsiUtils.isChild(element, List.of(STAGE)) && isNotSpecInputsElement(element);
  }

  public static boolean isExtendsElement(PsiElement element) {
    return PsiUtils.isChild(element, List.of(EXTENDS)) && isNotSpecInputsElement(element);
  }

  public static boolean isDependenciesElement(PsiElement element) {
    return PsiUtils.isChild(element, List.of(DEPENDENCIES)) && isNotSpecInputsElement(element);
  }

  public static boolean isNotSpecInputsElement(PsiElement element) {
    return !PsiUtils.isChild(element, List.of(SPEC, INPUTS));
  }
}
