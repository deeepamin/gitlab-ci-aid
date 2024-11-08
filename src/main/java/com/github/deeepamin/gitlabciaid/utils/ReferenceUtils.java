package com.github.deeepamin.gitlabciaid.utils;

import com.github.deeepamin.gitlabciaid.services.IncludeFileReferenceResolver;
import com.github.deeepamin.gitlabciaid.services.ScriptReferenceResolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Optional;

public class ReferenceUtils {
  public static Optional<PsiReference[]> referencesScripts(PsiElement element) {
    if (element instanceof YAMLPlainTextImpl) {
      return Optional.of(new PsiReference[]{new ScriptReferenceResolver(element)});
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static Optional<PsiReference[]> referencesIncludeLocalFiles(PsiElement element) {
    if (element instanceof YAMLPlainTextImpl) {
      return Optional.of(new PsiReference[]{new IncludeFileReferenceResolver(element)});
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }
}
