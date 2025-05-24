package com.github.deeepamin.ciaid.model;

import com.github.deeepamin.ciaid.model.gitlab.inputs.Input;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@SuppressWarnings("UnstableApiUsage")
public class InputDocumentationTarget implements DocumentationTarget {
  private final SmartPsiElementPointer<PsiElement> elementPtr;
  private final SmartPsiElementPointer<PsiElement> originalElementPtr;

  public InputDocumentationTarget(PsiElement element, PsiElement originalElement) {
    SmartPointerManager pointerManager = SmartPointerManager.getInstance(element.getProject());
    this.elementPtr = pointerManager.createSmartPsiElementPointer(element);
    this.originalElementPtr = pointerManager.createSmartPsiElementPointer(originalElement);
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
      return new InputDocumentationTarget(element, original);
    };
  }

  @Override
  public @NotNull TargetPresentation computePresentation() {
    var originalElement = originalElementPtr.getElement();
    if (originalElement != null) {
      var originalElementTextAndOffsets = GitlabCIYamlUtils.getInputNameFromInputsString(originalElement.getText());
      if (originalElementTextAndOffsets != null) {
        var originalElementText = originalElementTextAndOffsets.path();
        var input = CIAidProjectService.getInstance(originalElement.getProject())
                .getInputs().stream()
                .filter(inputInner -> inputInner.name().equals(originalElementText)).findFirst().orElse(null);

        if (input != null) {
          var inputFile = input.inputElement().getContainingFile();
          String inputFilePath = "";
          if (inputFile != null) {
            inputFilePath = inputFile.getVirtualFile().getPath();
          }
          var basePath = originalElement.getProject().getBasePath();
          if (basePath != null) {
            inputFilePath = inputFilePath.replace(basePath + File.separator, "");
          }
          return TargetPresentation.builder(input.name() + " Documentation")
                  .locationText(inputFilePath, Icons.ICON_YAML.getIcon())
                  .presentation();
        }
      }
    }
    return TargetPresentation.builder("").presentation();
  }

  @Override
  public @Nullable DocumentationResult computeDocumentation() {
    var originalElement = originalElementPtr.getElement();
    if (originalElement != null) {
      var originalElementTextAndOffsets = GitlabCIYamlUtils.getInputNameFromInputsString(originalElement.getText());
      if (originalElementTextAndOffsets != null) {
        var originalElementText = originalElementTextAndOffsets.path();
        var input = CIAidProjectService.getInstance(originalElement.getProject())
                .getInputs().stream()
                .filter(inputInner -> inputInner.name().equals(originalElementText)).findFirst().orElse(null);
        if (input != null) {
          var htmlChunk = buildHtmlChunk(input);
          return DocumentationResult.documentation(htmlChunk.toString());
        }
      }
    }
    return null;
  }

  private HtmlChunk buildHtmlChunk(Input input) {
    return HtmlChunk.fragment(
            HtmlChunk.div("font-family: JetBrains Mono; font-weight: bold; margin-bottom: 5px;").addRaw(input.name() + ": " + input.inputType().toString().toLowerCase()),
            HtmlChunk.hr(),
            HtmlChunk.div("margin-top: 5px; margin-bottom: 5px;").addRaw(input.description()),
            HtmlChunk.span("color: gray; margin-bottom: 8px;").addRaw("default: " + input.defaultValue())
    );
  }
}
