package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.StagesReferenceResolver;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.Objects;
import java.util.Optional;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class StagesReferenceProvider extends AbstractReferenceProvider {
  public StagesReferenceProvider(PsiElement element) {
    super(element);
  }

  @Override
  protected boolean isReferenceAvailable() {
    var isStagesElement = GitlabCIYamlUtils.isStagesElement(element);
    var isYamlTextElement = YamlUtils.isYamlTextElement(element);
    return isStagesElement && isYamlTextElement;
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    var stageName = handleQuotedText(element.getText());

    var targetJobs = ciAidProjectService.getPluginData().values()
            .stream()
            .flatMap(yamlData -> yamlData.getJobStageElements().stream())
            .filter(stage -> handleQuotedText(stage.getText()).equals(stageName))
            .map(stage -> {
              // Navigate from stage value to parent job: stage -> YAMLKeyValue(stage:) -> YAMLBlockMapping -> YAMLKeyValue(job)
              PsiElement parent = stage.getParent();
              if (parent != null) {
                parent = parent.getParent();
                if (parent != null) {
                  parent = parent.getParent();
                }
              }
              return parent;
            })
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    if (!targetJobs.isEmpty()) {
      return Optional.of(new PsiReference[]{ new StagesReferenceResolver(element, targetJobs) });
    }

    var targetStages = ciAidProjectService.getPluginData().values()
            .stream()
            .flatMap(yamlData -> yamlData.getJobStageElements().stream())
            .filter(stage -> handleQuotedText(stage.getText()).equals(stageName))
            .toList();
    return Optional.of(new PsiReference[]{ new StagesReferenceResolver(element, targetStages) });
  }
}
