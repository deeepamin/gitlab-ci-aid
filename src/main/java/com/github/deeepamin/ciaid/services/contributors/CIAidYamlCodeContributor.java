package com.github.deeepamin.ciaid.services.contributors;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class CIAidYamlCodeContributor extends CompletionContributor {
  public CIAidYamlCodeContributor() {
    extend(CompletionType.BASIC, psiElement(), completionProvider());
  }

  private CompletionProvider<CompletionParameters> completionProvider() {
    return new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        var psiElement = parameters.getPosition();
        var isGitlabYamlFile = GitlabCIYamlUtils.isValidGitlabCIYamlFile(psiElement.getContainingFile().getVirtualFile());
        if (!isGitlabYamlFile) {
          return;
        }

        var isChildOfScript = PsiUtils.isScriptElement(psiElement);
        if (isChildOfScript) {
          if (isAfterDollarSign(psiElement)) {
            CIAidProjectService.getInstance(psiElement.getProject())
                    .getPluginData()
                    .values()
                    .stream()
                    .flatMap(data -> data.getVariables().stream())
                    .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
                    .map(SmartPsiElementPointer::getElement)
                    .map(YAMLKeyValue::getKeyText)
                    .forEach(variable -> result.addElement(LookupElementBuilder.create(variable)
                            .bold()));
          }
        }
      }
    };
  }

  private boolean isAfterDollarSign(PsiElement position) {
    var prev = PsiTreeUtil.prevVisibleLeaf(position);
    if (prev == null) return false;

    String prevText = prev.getText();
    String text = prevText + position.getText();
    return text.matches("\\$\\{?}?");
  }
}
