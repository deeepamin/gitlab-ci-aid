package com.github.deeepamin.ciaid.services.contributors;

import com.github.deeepamin.ciaid.model.GitlabCIYamlData;
import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.getGitlabCIYamlProjectService;
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
                  boolean isExtendsElement = PsiUtils.isExtendsElement(psiElement);
                  if (isNeedsElement || isExtendsElement) {
                    var allJobs = getGitlabCIYamlProjectService(psiElement).getJobNames();
                    var parentJob = PsiUtils.findParent(psiElement, allJobs);
                    List<String> filteredJobs = new ArrayList<>(allJobs);
                    parentJob.ifPresent(job -> filteredJobs.remove(job.getName()));
                    BiPredicate<Map.Entry<VirtualFile, GitlabCIYamlData>, String> jobFilterPredicate = (entry, job) -> {
                      boolean isKnownJob = entry.getValue().getJobs().containsKey(job);
                      if (isNeedsElement) {
                        isKnownJob = isKnownJob && !job.startsWith(".");
                      }
                      return isKnownJob;
                    };
                    result.addAllElements(filteredJobs.stream()
                            .map(job -> LookupElementBuilder.create(job)
                                    .bold()
                                    .withIcon(Icons.ICON_NEEDS.getIcon())
                                    .withTypeText(getGitlabCIYamlProjectService(psiElement).getFileName(psiElement.getProject(), entry -> jobFilterPredicate.test(entry, job))))
                            .toList());
//                    return;
                  }
                  boolean isStageElement = PsiUtils.isStageElement(psiElement);
                  if (isStageElement) {
                    // on stage element show suggestions from top level stages
                    var allStages = getGitlabCIYamlProjectService(psiElement).getStageNamesDefinedAtStagesLevel();
                    result.addAllElements(allStages.stream()
                            .map(stage -> LookupElementBuilder.create(stage)
                                    .bold()
                                    .withIcon(Icons.ICON_STAGE.getIcon())
                                    .withTypeText(getGitlabCIYamlProjectService(psiElement).getFileName(psiElement.getProject(), (entry) -> entry.getValue().getStages().containsKey(stage))))
                            .toList());
//                    return;
                  }
                  boolean isStagesElement = PsiUtils.isStagesElement(psiElement);
                  if (isStagesElement) {
                    // on top level stages show suggestion from job level
                    var allStages = getGitlabCIYamlProjectService(psiElement).getStageNamesDefinedAtJobLevel();
                    result.addAllElements(allStages.stream()
                            .map(stage -> LookupElementBuilder.create(stage)
                                    .bold()
                                    .withIcon(Icons.ICON_STAGE.getIcon())
                                    .withTypeText(getGitlabCIYamlProjectService(psiElement).getFileName(psiElement.getProject(), (entry) -> entry.getValue().getStages().containsKey(stage))))
                            .toList());
                  }
                });

      }
    };
  }

}
