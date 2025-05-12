package com.github.deeepamin.ciaid.services.contributors;

import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.ReferenceUtils;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Optional;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class CIAidYamlReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
    psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.or(
                    psiElement(YAMLScalar.class)
            ),
            new PsiReferenceProvider() {
              @Override
              public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext context) {
                return !GitlabCIYamlUtils.hasGitlabYamlFile(psiElement)
                        ? PsiReference.EMPTY_ARRAY
                        : Optional.of(psiElement).flatMap(ReferenceUtils::getReferences).orElse(PsiReference.EMPTY_ARRAY);
              }
            }, PsiReferenceRegistrar.DEFAULT_PRIORITY
    );
  }
}
