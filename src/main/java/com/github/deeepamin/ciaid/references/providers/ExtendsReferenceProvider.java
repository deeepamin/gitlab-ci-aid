package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.ExtendsReferenceResolver;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.Optional;

public class ExtendsReferenceProvider extends NeedsReferenceProvider {
  public ExtendsReferenceProvider(PsiElement element) {
    super(element);
  }

  @Override
  protected boolean isReferenceAvailable() {
    var isExtendsElement = GitlabCIYamlUtils.isExtendsElement(element);
    var isTextElement = YamlUtils.isYamlTextElement(element);
    return isExtendsElement && isTextElement;
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    return Optional.of(new PsiReference[]{ new ExtendsReferenceResolver(element, getTargetElement()) });
  }
}
