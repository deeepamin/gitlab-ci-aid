package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.RefTagReferenceResolver;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.impl.YAMLArrayImpl;

import java.util.Optional;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class RefTagReferenceProvider extends AbstractReferenceProvider {
  public RefTagReferenceProvider(PsiElement element) {
    super(element);
  }

  public static String getReferenceTag(YAMLPsiElement element) {
    var parent = PsiUtils.findParentOfType(element, YAMLArrayImpl.class);
    if (parent.isEmpty()) {
      return null;
    }
    var firstChild = parent.get().getFirstChild();
    if (!GitlabCIYamlUtils.REFERENCE_TAG.equals(firstChild.getText())) {
      return null;
    }
    var children = parent.get().getChildren();
    if (children.length > 0) {
      var refersToText = handleQuotedText(children[0].getText());
      if (element.getText() != null && element.getText().equals(refersToText)) {
        return refersToText;
      }
      if (children.length > 1) {
        var keyToReferToText = handleQuotedText(children[1].getText());
        if (element.getText() != null && element.getText().equals(keyToReferToText)) {
          // still return refersToText, as it is the reference tag and the key is resolved in resolver
          return refersToText;
        }
      }
    }
    return null;
  }

  @Override
  protected boolean isReferenceAvailable() {
    return YamlUtils.isYamlPsiElement(element);
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    var refTagText = RefTagReferenceProvider.getReferenceTag((YAMLPsiElement) element);
    if (refTagText == null) {
      return Optional.of(PsiReference.EMPTY_ARRAY);
    }
    // reference tag is like !reference [.some_job, key], and .some_job is a top level element so it is inside jobs
    var refersTo = ciAidProjectService.getPluginData().values()
            .stream()
            .flatMap(yamlData -> yamlData.getJobElements().stream())
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(SmartPsiElementPointer::getElement)
            .filter(job -> handleQuotedText(job.getKeyText()).equals(refTagText))
            .findFirst()
            .orElse(null);

    var elementText = element.getText();
    // !reference [.some_job, key]: the element could be either .some_job or key
    if (refTagText.equals(elementText)) {
      return Optional.of(new PsiReference[]{ new RefTagReferenceResolver(element, refersTo) });
    } else {
      var keyToReferTo = PsiUtils.findChildWithKey(refersTo, elementText);
      return Optional.of(new PsiReference[]{ new RefTagReferenceResolver(element, keyToReferTo) });
    }
  }
}
