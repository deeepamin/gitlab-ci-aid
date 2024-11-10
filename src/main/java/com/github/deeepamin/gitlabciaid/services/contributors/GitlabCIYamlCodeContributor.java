package com.github.deeepamin.gitlabciaid.services.contributors;

import com.github.deeepamin.gitlabciaid.model.Icons;
import com.github.deeepamin.gitlabciaid.services.GitlabCIYamlApplicationService;
import com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class GitlabCIYamlCodeContributor extends CompletionContributor {
  public GitlabCIYamlCodeContributor() {
    extend(CompletionType.BASIC, psiElement(), completionProvider());
  }

  private CompletionProvider<CompletionParameters> completionProvider() {
    return new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        var psiElement = parameters.getPosition();
        Optional.of(GitlabCIYamlUtils.isValidGitlabCIYamlFile(psiElement.getContainingFile().getVirtualFile()))
                .ifPresent(file -> {
                  boolean isNeedsElement = PsiUtils.isNeedsElement(psiElement);
                  if (isNeedsElement) {
                    var allJobs = GitlabCIYamlApplicationService.getJobNames();
                    result.addAllElements(allJobs.stream()
                            .map(job -> LookupElementBuilder.create(job)
                                    .bold()
                                    .withIcon(Icons.ICON_NEEDS.getIcon())
                                    .withTypeText(GitlabCIYamlApplicationService.getFileName(psiElement.getProject(), (entry) -> entry.getValue().getJobs().containsKey(job))))
                            .toList());
                  }
                  boolean isStageElement = PsiUtils.isStageElement(psiElement);
                  if (isStageElement) {
                    // on stage element show suggestions from top level stages
                    var allStages = GitlabCIYamlApplicationService.getStageNamesDefinedAtStagesLevel();
                    result.addAllElements(allStages.stream()
                            .map(stage -> LookupElementBuilder.create(stage)
                                    .bold()
                                    .withIcon(Icons.ICON_STAGE.getIcon())
                                    .withTypeText(GitlabCIYamlApplicationService.getFileName(psiElement.getProject(), (entry) -> entry.getValue().getStages().containsKey(stage))))
                            .toList());
                  }
                  boolean isStagesElement = PsiUtils.isStagesElement(psiElement);
                  if (isStagesElement) {
                    // on top level stages show suggestion from job level
                    var allStages = GitlabCIYamlApplicationService.getStageNamesDefinedAtJobLevel();
                    result.addAllElements(allStages.stream()
                            .map(stage -> LookupElementBuilder.create(stage)
                                    .bold()
                                    .withIcon(Icons.ICON_STAGE.getIcon())
                                    .withTypeText(GitlabCIYamlApplicationService.getFileName(psiElement.getProject(), (entry) -> entry.getValue().getStages().containsKey(stage))))
                            .toList());
                  }
                });

      }
    };
  }

}
