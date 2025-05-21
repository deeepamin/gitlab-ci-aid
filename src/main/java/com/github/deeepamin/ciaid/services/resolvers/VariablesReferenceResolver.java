package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;

public class VariablesReferenceResolver extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
  private final List<YAMLKeyValue> targets;

  public VariablesReferenceResolver(@NotNull PsiElement element, List<YAMLKeyValue> targets, TextRange rangeInElement) {
    super(element, rangeInElement);
    this.targets = targets;
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    if (targets != null) {
      return targets.stream()
              .map(PsiElementResolveResult::new)
              .toList()
              .toArray(ResolveResult[]::new);
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  @Override
  public @Nullable PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return myElement.getText();
  }

  @Override
  public Object @NotNull [] getVariants() {
    var varsToCompleteWithFileNames = CIAidProjectService.getInstance(myElement.getProject()).getVariableAndContainingFiles();
    return varsToCompleteWithFileNames.entrySet().stream()
            .map((variableAndFileName) -> {
              var variable = variableAndFileName.getKey();
              var files = variableAndFileName.getValue();
              var fileNameText = files.size() == 1 ? files.getFirst().getName() : "(" + CIAidBundle.message("variables.completion.declared.multiple.files") +")";
              return LookupElementBuilder.create(variable)
                      .bold()
                      .withIcon(Icons.ICON_VARIABLE.getIcon())
                      .withTypeText(fileNameText);
            }).toArray();
  }
}
