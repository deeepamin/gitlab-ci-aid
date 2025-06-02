package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.InputsReferenceResolver;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.SmartPsiElementPointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class InputsReferenceProvider extends AbstractReferenceProvider {
  public InputsReferenceProvider(PsiElement element) {
    super(element);
  }

  public static List<FileUtils.StringWithStartEndRange> getInputNames(String input) {
    if (input == null) {
      return null;
    }
    var pattern = Pattern.compile("\\$\\[\\[\\s*inputs\\.(\\w+)");
    var matcher = pattern.matcher(input);
    var inputs = new ArrayList<FileUtils.StringWithStartEndRange>();
    while (matcher.find()) {
      inputs.add(new FileUtils.StringWithStartEndRange(matcher.group(1), matcher.start(1), matcher.end(1)));
    }
    return inputs;
  }

  @Override
  protected boolean isReferenceAvailable() {
    return YamlUtils.isYamlPsiElement(element);
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    var elementText = element.getText();
    var inputNamesWithStartEndRange = InputsReferenceProvider.getInputNames(elementText);
    if (inputNamesWithStartEndRange == null || inputNamesWithStartEndRange.isEmpty()) {
      return Optional.of(PsiReference.EMPTY_ARRAY);
    }

    var inputRefs = new ArrayList<PsiReference>();
    inputNamesWithStartEndRange.forEach(inputNameWithStartEndRange -> {
      var inputName = inputNameWithStartEndRange.path();
      var startOffset = inputNameWithStartEndRange.start();
      var endOffset = inputNameWithStartEndRange.end();

      var targetInput = ciAidProjectService.getPluginData().values()
              .stream()
              .flatMap(yamlData -> yamlData.getInputs().stream())
              .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
              .map(SmartPsiElementPointer::getElement)
              .filter(inputKeyValue -> inputKeyValue.getKeyText().equals(inputName))
              .findFirst()
              .orElse(null);

      inputRefs.add(new InputsReferenceResolver(element, targetInput, TextRange.create(startOffset, endOffset)));
    });
    return Optional.of(inputRefs.toArray(PsiReference[]::new));
  }
}
