package com.github.deeepamin.ciaid.documentation.targets;

import com.intellij.model.Pointer;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.deeepamin.ciaid.utils.FileUtils.StringWithStartEndRange;

public class MultipleInputsDocumentationTarget extends InputDocumentationTarget {
  // for cases where multiple inputs are defined in the same line e.g. $[[ inputs.input1 ]] / $[[ inputs.input2 ]]
  private final Project project;
  private final StringWithStartEndRange inputWithTextRange;

  public MultipleInputsDocumentationTarget(PsiElement element, PsiElement originalElement, StringWithStartEndRange inputWithTextRange) {
    super(element, originalElement);
    this.project = originalElement.getProject();
    this.inputWithTextRange = inputWithTextRange;
  }

  @Override
  public @NotNull Pointer<? extends DocumentationTarget> createPointer() {
    SmartPsiElementPointer<PsiElement> elementPtrCopy = this.elementPtr;
    SmartPsiElementPointer<PsiElement> originalElementPtrCopy = this.originalElementPtr;

    return (Pointer<DocumentationTarget>) () -> {
      PsiElement element = elementPtrCopy.getElement();
      PsiElement original = originalElementPtrCopy.getElement();
      if (element == null || original == null) {
        return null;
      }
      return new MultipleInputsDocumentationTarget(element, original, inputWithTextRange);
    };
  }

  @Override
  public @NotNull TargetPresentation computePresentation() {
    var input = getInput(project, this.inputWithTextRange);
    return getTargetPresentation(input);
  }

  @Override
  public @Nullable DocumentationResult computeDocumentation() {
    var input = getInput(project, this.inputWithTextRange);
    return getDocumentationResult(input);
  }
}
