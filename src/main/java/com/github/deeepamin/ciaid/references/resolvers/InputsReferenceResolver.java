package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.model.Icons;
import com.github.deeepamin.ciaid.model.gitlab.inputs.Input;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class InputsReferenceResolver extends SingleTargetReferenceResolver {
  private static final String INPUT_APPEND_STRING = " ]]";
  private static final String INPUT_APPEND_STRING_TRIMMED = "]]";

  // From Inputs element to spec:inputs
  public InputsReferenceResolver(@NotNull PsiElement element, PsiElement target, TextRange textRange) {
    super(element, target, textRange);
  }

  @Override
  public Object @NotNull [] getVariants() {
    return CIAidProjectService.getInstance(myElement.getProject())
            .getDataProvider()
            .getInputs()
            .stream()
            .map(input -> LookupElementBuilder.create(input.name())
                            .bold()
                            .withIcon(getIcon(input))
                            .withTypeText(input.description())
                            .withInsertHandler((context, item) -> {
                              int tailOffset = context.getTailOffset();
                              String fullText = context.getDocument().getText();
                              if (!fullText.startsWith(INPUT_APPEND_STRING_TRIMMED, tailOffset)
                                      && !fullText.startsWith(INPUT_APPEND_STRING, tailOffset)) {
                                context.getDocument().insertString(tailOffset, INPUT_APPEND_STRING);
                                context.getEditor().getCaretModel().moveToOffset(tailOffset);
                              }
                            }))
            .toArray(LookupElement[]::new);
  }

  private Icon getIcon(Input input) {
    Icons icons = switch (input.inputType()) {
      case BOOLEAN -> Icons.ICON_BOOLEAN;
      case NUMBER -> Icons.ICON_NUMBER;
      case STRING -> Icons.ICON_STRING;
      case ARRAY -> Icons.ICON_ARRAY;
    };
    return icons.getIcon();
  }
}
