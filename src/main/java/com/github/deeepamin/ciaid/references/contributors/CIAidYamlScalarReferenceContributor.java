package com.github.deeepamin.ciaid.references.contributors;

import com.github.deeepamin.ciaid.references.providers.AbstractReferenceProvider;
import com.github.deeepamin.ciaid.references.providers.ReferenceProviderFactory;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Arrays;
import java.util.Optional;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class CIAidYamlScalarReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
    psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.or(
                    psiElement(YAMLScalar.class)
            ),
            new PsiReferenceProvider() {
              @Override
              public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext context) {
                if (!GitlabCIYamlUtils.hasGitlabYamlFile(psiElement)) {
                  return PsiReference.EMPTY_ARRAY;
                }
                var referenceProviders = ReferenceProviderFactory.getYamlScalarReferenceProviders(psiElement);
                return referenceProviders.stream()
                        .map(AbstractReferenceProvider::getElementReferences)
                        .flatMap(Optional::stream)
                        .filter(reference -> reference != null && reference.length > 0)
                        .flatMap(Arrays::stream)
                        .toArray(PsiReference[]::new);
              }
            }, PsiReferenceRegistrar.DEFAULT_PRIORITY
    );
  }
}
