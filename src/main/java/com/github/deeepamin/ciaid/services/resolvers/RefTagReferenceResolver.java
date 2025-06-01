package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLArrayImpl;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import java.util.Arrays;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;
import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.REFERENCE_TAG;

public class RefTagReferenceResolver extends SingleTargetReferenceResolver {
  public RefTagReferenceResolver(@NotNull PsiElement element, PsiElement target) {
    super(element, target);
  }

  @Override
  public Object @NotNull [] getVariants() {
    var ciAidProjectService = CIAidProjectService.getInstance(myElement.getProject());
    var referencesToShow = ciAidProjectService.getPluginData()
            .values()
            .stream()
            .flatMap(ciAidYamlData -> ciAidYamlData.getJobElements().stream())
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(SmartPsiElementPointer::getElement)
            .filter(keyValue -> handleQuotedText(keyValue.getKeyText()).startsWith("."))
            .toList();
    var parent = PsiUtils.findParentOfType(myElement, YAMLArrayImpl.class);
    if (parent.isPresent()) {
      var firstChild = parent.get().getFirstChild();
      if (firstChild != null && firstChild.getText().equals(REFERENCE_TAG)) {
        var children = parent.get().getChildren();
        if (children.length == 2) {
          var elementText = myElement.getText();
          if (elementText.equals(children[0].getText())) {
            return referencesToShow.stream()
                    .map(YAMLKeyValue::getKeyText)
                    .map(CIAidUtils::handleQuotedText)
                    .map(ref -> LookupElementBuilder.create(ref)
                            .bold()
                            .withIcon(Icons.ICON_NEEDS.getIcon())
                            .withTypeText(ciAidProjectService.getJobFileName(ref))
                    ).toArray(LookupElement[]::new);
          } else {
            var referenceText = handleQuotedText(children[0].getText());
            var refersTo = referencesToShow.stream()
                    .filter(keyValue -> handleQuotedText(keyValue.getKeyText()).equals(referenceText))
                    .findFirst()
                    .orElse(null);
            if (refersTo != null) {
              var refersToChildren = refersTo.getChildren();
              for (var refersToChild : refersToChildren) {
                if (refersToChild instanceof YAMLBlockMappingImpl yamlBlockMapping) {
                  return Arrays.stream(yamlBlockMapping.getChildren())
                          .filter(child -> child instanceof YAMLKeyValue)
                          .map(child -> (YAMLKeyValue) child)
                          .map(YAMLKeyValue::getKeyText)
                          .map(CIAidUtils::handleQuotedText)
                          .map(ref -> LookupElementBuilder.create(ref)
                                  .bold()
                                  .withTypeText(ciAidProjectService.getJobFileName(referenceText))
                          ).toArray(LookupElement[]::new);
                }
              }
            }
          }
        }
      }
    }
    return new LookupElement[0];
  }

}
