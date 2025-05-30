package com.github.deeepamin.ciaid.services.targets;

import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.model.gitlab.Input;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.model.Pointer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.github.deeepamin.ciaid.utils.FileUtils.StringWithStartEndRange;

@SuppressWarnings("UnstableApiUsage")
public class InputDocumentationTarget implements DocumentationTarget {
  protected final SmartPsiElementPointer<PsiElement> elementPtr;
  protected final SmartPsiElementPointer<PsiElement> originalElementPtr;

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
      var inputNamesAndOffsets = GitlabCIYamlUtils.getInputNames(originalElement.getText());
      if (inputNamesAndOffsets != null && inputNamesAndOffsets.size() == 1) {
        var inputNameAndOffsets = inputNamesAndOffsets.getFirst();
        var input = getInput(originalElement.getProject(), inputNameAndOffsets);
        return getTargetPresentation(input);
      }
    }
    return TargetPresentation.builder("").presentation();
  }

  @Override
  public @Nullable DocumentationResult computeDocumentation() {
    var originalElement = originalElementPtr.getElement();
    if (originalElement != null) {
      var inputNamesAndOffsets = GitlabCIYamlUtils.getInputNames(originalElement.getText());
      if (inputNamesAndOffsets != null && inputNamesAndOffsets.size() == 1) {
        var inputNameAndOffsets = inputNamesAndOffsets.getFirst();
        var input = getInput(originalElement.getProject(), inputNameAndOffsets);
        return getDocumentationResult(input);
      }
    }
    return null;
  }

  protected Input getInput(Project project, StringWithStartEndRange inputTextWithStartEndRange) {
    if (inputTextWithStartEndRange == null) {
      return null;
    }
    var inputName = inputTextWithStartEndRange.path();
    return CIAidProjectService.getInstance(project)
            .getInputs().stream()
            .filter(inputInner -> inputInner.name().equals(inputName)).findFirst().orElse(null);
  }

  protected TargetPresentation getTargetPresentation(Input input) {
    if (input != null) {
      var inputFile = input.inputElement().getContainingFile();
      if (inputFile != null) {
        var inputFilePath = getFilePathForDocumentation(originalElementPtr.getProject(), inputFile.getVirtualFile());
        if (inputFilePath != null) {
          return TargetPresentation.builder(input.name() + " Documentation")
                  .locationText(inputFilePath, Icons.ICON_YAML.getIcon())
                  .presentation();
        }
      }
    }
    return TargetPresentation.builder("").presentation();
  }

  protected DocumentationResult getDocumentationResult(Input input) {
    if (input != null) {
      var htmlChunk = buildHtmlChunk(input);
      return DocumentationResult.documentation(htmlChunk.toString());
    }
    return null;
  }

  private String getFilePathForDocumentation(Project project, VirtualFile virtualFile) {
    if (virtualFile != null) {
      var filePath = virtualFile.getPath();
      var basePath = project.getBasePath();
      if (basePath != null && filePath.startsWith(basePath + File.separator)) {
        return filePath.replace(basePath + File.separator, "");
      } else {
        return filePath;
      }
    }
    return "";
  }

  private HtmlChunk buildHtmlChunk(Input input) {
    return HtmlChunk.fragment(
            HtmlChunk.div("font-family: JetBrains Mono; font-weight: bold; margin-bottom: 5px;").addRaw(input.name() + ": " + input.inputType().toString().toLowerCase()),
            HtmlChunk.hr(),
            HtmlChunk.div("margin-top: 5px; margin-bottom: 5px;").addRaw(input.description()),
            HtmlChunk.span("color: gray; margin-bottom: 8px;").addRaw("Default: " + input.defaultValue())
    );
  }
}
