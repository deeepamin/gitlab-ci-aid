package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.JobStageReferenceResolver;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.Optional;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class JobStageReferenceProvider extends AbstractReferenceProvider {
  public JobStageReferenceProvider(PsiElement element) {
    super(element);
  }

  @Override
  protected boolean isReferenceAvailable() {
    var isJobStageElement = PsiUtils.isJobStageElement(element);
    var isYamlTextElement = YamlUtils.isYamlTextElement(element);
    return isJobStageElement && isYamlTextElement;
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    var jobStageName = handleQuotedText(element.getText());

    var target = ciAidProjectService.getPluginData().values()
            .stream()
            .flatMap(yamlData -> yamlData.getStagesItemElements().stream())
            .filter(stage -> handleQuotedText(stage.getText()).equals(jobStageName))
            .findFirst()
            .orElse(null);
    return Optional.of(new PsiReference[]{ new JobStageReferenceResolver(element, target) });
  }
}
