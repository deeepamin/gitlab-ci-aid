package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.VariablesReferenceResolver;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class VariablesReferenceProvider extends AbstractReferenceProvider {
  public VariablesReferenceProvider(PsiElement element) {
    super(element);
  }

  public static List<FileUtils.StringWithStartEndRange> getVariables(String text) {
    if (text == null) {
      return null;
    }
    var vars = new ArrayList<FileUtils.StringWithStartEndRange>();
    var pattern = Pattern.compile("\\$\\{?(\\w+)}?");
    var matcher = pattern.matcher(text);
    while (matcher.find()) {
      vars.add(new FileUtils.StringWithStartEndRange(matcher.group(1), matcher.start(1), matcher.end(1)));
    }
    return vars;
  }

  @Override
  protected boolean isReferenceAvailable() {
    return YamlUtils.isYamlPsiElement(element);
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    var elementText = element.getText();
    var variables = getVariables(elementText);
    if (variables.isEmpty()) {
      return Optional.of(PsiReference.EMPTY_ARRAY);
    }
    var psiRefs = new ArrayList<PsiReference>();
    variables.forEach(variableWithStartEndRange -> {
      var variableName = variableWithStartEndRange.path();
      var startOffset = variableWithStartEndRange.start();
      var endOffset = variableWithStartEndRange.end();

      var targetVariables = ciAidProjectService.getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getVariables().stream())
              .filter(variableKeyValue -> variableKeyValue.getKeyText().equals(variableName))
              .toList();
      psiRefs.add(new VariablesReferenceResolver(element, targetVariables, TextRange.create(startOffset, endOffset)));
    });
    return Optional.of(psiRefs.toArray(PsiReference[]::new));
  }
}
