package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.IncludeFileResolverFactory;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.Optional;

public class IncludeReferenceProvider extends AbstractReferenceProvider {
  public IncludeReferenceProvider(PsiElement element) {
    super(element);
  }

  @Override
  protected boolean isReferenceAvailable() {
    var isIncludeElement = GitlabCIYamlUtils.isIncludeElement(element);
    var isYamlTextElement = YamlUtils.isYamlTextElement(element);
    return isIncludeElement && isYamlTextElement;
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    var includeFileResolver = IncludeFileResolverFactory.getIncludeFileResolver(element);
    if (includeFileResolver == null) {
      return Optional.of(PsiReference.EMPTY_ARRAY);
    }
    return Optional.of(new PsiReference[]{ includeFileResolver });
  }

}
