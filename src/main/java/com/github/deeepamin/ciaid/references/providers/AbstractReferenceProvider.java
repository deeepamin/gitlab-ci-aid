package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.Optional;

public abstract class AbstractReferenceProvider {
  protected final Logger LOG = Logger.getInstance(getClass());
  protected final Project project;
  protected final PsiElement element;
  protected final CIAidProjectService ciAidProjectService;

  protected AbstractReferenceProvider(PsiElement element) {
    this.element = element;
    this.project = element.getProject();
    this.ciAidProjectService = CIAidProjectService.getInstance(project);
  }

  protected abstract boolean isReferenceAvailable();

  public final Optional<PsiReference[]> getElementReferences() {
    if (isReferenceAvailable()) {
      return getReferences();
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  protected abstract Optional<PsiReference[]> getReferences();
}
