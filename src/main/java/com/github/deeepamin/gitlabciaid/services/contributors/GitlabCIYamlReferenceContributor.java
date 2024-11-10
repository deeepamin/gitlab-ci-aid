package com.github.deeepamin.gitlabciaid.services.contributors;

import com.github.deeepamin.gitlabciaid.utils.ReferenceUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils.getGitlabCIYamlFile;
import static com.intellij.patterns.PlatformPatterns.psiElement;

public class GitlabCIYamlReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
    psiReferenceRegistrar.registerReferenceProvider(
            psiElement(YAMLPlainTextImpl.class),
            new PsiReferenceProvider() {
              @Override
              public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext context) {
//                System.out.println(psiElement.getText() + " is of type " + psiElement.getClass().getName());
                return getGitlabCIYamlFile(psiElement).isEmpty() ? PsiReference.EMPTY_ARRAY : Optional.of(psiElement)
                        .flatMap(ReferenceUtils::getReferences)
                        .orElse(PsiReference.EMPTY_ARRAY);
              }
            }, PsiReferenceRegistrar.LOWER_PRIORITY
    );
  }
}
