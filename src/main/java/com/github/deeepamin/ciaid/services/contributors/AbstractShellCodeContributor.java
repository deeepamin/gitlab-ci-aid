package com.github.deeepamin.ciaid.services.contributors;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.intellij.patterns.PlatformPatterns.psiElement;

abstract class AbstractShellCodeContributor extends CompletionContributor {
  private final @Nullable Language language;

  protected AbstractShellCodeContributor(@NotNull String languageId) {
    this.language = Language.findLanguageByID(languageId);
    if (language != null) {
      extend(CompletionType.BASIC, psiElement().withLanguage(language), completionProvider());
    }
  }


  private CompletionProvider<CompletionParameters> completionProvider() {
    return new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        var project = parameters.getPosition().getProject();

        PsiElement injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(parameters.getPosition());
        if (injectionHost == null) {
          return;
        }

        var originalFile = injectionHost.getContainingFile().getOriginalFile();
        if (GitlabCIYamlUtils.isValidGitlabCIYamlFile(originalFile.getVirtualFile())) {
          var psiElement = parameters.getPosition();
          if (psiElement.getLanguage() == language) {
            var isVariable = isVariableString(psiElement);
            if (isVariable) {
              result = result.withPrefixMatcher(CompletionUtil.findReferenceOrAlphanumericPrefix(parameters));
              var varsToCompleteWithFileNames = CIAidProjectService.getInstance(project).getVariableAndContainingFiles();

              for (Map.Entry<String, List<VirtualFile>> variableAndFileName : varsToCompleteWithFileNames.entrySet()) {
                var variable = variableAndFileName.getKey();
                var files = variableAndFileName.getValue();
                var fileNameText = files.size() == 1 ? files.getFirst().getName() : "(" + CIAidBundle.message("variables.completion.declared.multiple.files") +")";
                result.addElement(LookupElementBuilder.create(variable)
                        .bold()
                        .withIcon(Icons.ICON_VARIABLE.getIcon())
                        .withTypeText(fileNameText));
              }
            }
          }
        }
      }
    };
  }

  private static boolean isVariableString(PsiElement psiElement) {
    if (psiElement == null || psiElement.getText() == null) {
      return false;
    }
    var psiElementText = psiElement.getText();
    var isVariable = false;
    var isOnlyDollarSignVar = psiElementText.matches("\\$\\{?(\\w+)\\s*}?");
    if (isOnlyDollarSignVar) {
      isVariable = true;
    } else {
      if (psiElement.getParent() == null || psiElement.getParent().getParent() == null ||
              psiElement.getParent().getParent().getText() == null) {
        return false;
      }
      // when variable is typed like ${varName} shell psi element hierarchy: varName -> {varName} -> ${varName}, so get parent's parent
      var superParent = psiElement.getParent().getParent();
      var superParentText = superParent.getText();
      isVariable = superParentText.matches("\\$\\{?(\\w+)\\s*}?");
    }
    return isVariable;
  }
}
