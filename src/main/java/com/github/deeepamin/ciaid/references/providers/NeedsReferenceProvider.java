package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.NeedsReferenceResolver;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.Optional;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class NeedsReferenceProvider extends AbstractReferenceProvider {
  public NeedsReferenceProvider(PsiElement element) {
    super(element);
  }

  @Override
  protected boolean isReferenceAvailable() {
    var isNeedsElement = PsiUtils.isNeedsElement(element);
    var isTextElement = YamlUtils.isYamlTextElement(element);
    return isNeedsElement && isTextElement;
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    return Optional.of(new PsiReference[]{ new NeedsReferenceResolver(element, getTargetElement()) });
  }

  protected PsiElement getTargetElement() {
    // for cases: needs: ["some_job"] / needs: ["some_job] / needs: [some_job"]
    var targetName = handleQuotedText(element.getText());
    return ciAidProjectService.getPluginData().values()
            .stream()
            .flatMap(yamlData -> yamlData.getJobElements().stream())
            .filter(job -> handleQuotedText(job.getKeyText()).equals(targetName))
            .findFirst()
            .orElse(null);
  }
}
