package com.github.deeepamin.ciaid.references.providers;

import com.intellij.psi.PsiElement;

import java.util.List;

public class ReferenceProviderFactory {
  public static List<AbstractReferenceProvider> getYamlScalarReferenceProviders(PsiElement element) {
    return List.of(
            new ExtendsReferenceProvider(element),
            new DependenciesReferenceProvider(element),
            new IncludeReferenceProvider(element),
            new JobStageReferenceProvider(element),
            new NeedsReferenceProvider(element),
            new ScriptLocalFileReferenceProvider(element),
            new StagesReferenceProvider(element)
          );
  }

  public static List<AbstractReferenceProvider> getYamlPsiElementReferenceProviders(PsiElement element) {
    return List.of(
            new InputsReferenceProvider(element),
            new RefTagReferenceProvider(element),
            new VariablesReferenceProvider(element)
    );
  }
}
