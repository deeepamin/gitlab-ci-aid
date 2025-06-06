package com.github.deeepamin.ciaid.injectors.intentionActions;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalarText;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.isScriptElement;

public class ConvertFoldedToLiteralScriptBlockIntention extends PsiElementBaseIntentionAction {

  @Override
  public @NotNull @IntentionFamilyName String getFamilyName() {
    return CIAidBundle.message("intentions.yaml.scalar.block.conversion.family.name");
  }

  @Override
  public @NotNull @IntentionName String getText() {
    return CIAidBundle.message("intentions.yaml.folded.to.literal.script.block");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    // we get LeafPsiElement here, so we need to get the parent
    var parent = element.getParent();
    if (!(parent instanceof YAMLScalarText scalar)) {
      return;
    }
    if (!isFoldedBlock(scalar)) {
      return;
    }

    var superParent = parent.getParent();
    if (superParent instanceof YAMLKeyValue) {
      // direct in script block
      replaceKeyValue(scalar, superParent);
    } else if (superParent instanceof YAMLSequenceItem) {
      // multiple items, folded block is one of them
      replaceSequenceItem(scalar, superParent);
    }
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    var isGitLabCIYaml = CIAidProjectService.hasGitlabYamlFile(element);
    if (!isGitLabCIYaml) {
      return false;
    }
    var isScriptElement = isScriptElement(element);
    if (!isScriptElement) {
      return false;
    }
    var parent = element.getParent();
    if (element instanceof YAMLScalarText scalarText) {
      return isFoldedBlock(scalarText);
    } else if (parent instanceof YAMLScalarText scalarText) {
      return isFoldedBlock(scalarText);
    }
    return false;
  }

  private boolean isFoldedBlock(YAMLScalarText scalar) {
    if (scalar == null) {
      return false;
    }
    String text = scalar.getText();
    return text != null && text.startsWith(">");
  }

  private void replaceKeyValue(YAMLScalarText scalar, PsiElement superParent) {
    var scriptKeyValue = PsiTreeUtil.getParentOfType(scalar, YAMLKeyValue.class);
    if (scriptKeyValue == null) {
      return;
    }
    var key = scriptKeyValue.getKeyText();
    var content = scalar.getTextValue();
    var newTextBuilder = new StringBuilder(key + ": |\n");

    String[] lines = content.split("\\R");
    int last = lines.length - 1;
    for (int i = 0; i < lines.length; i++) {
      newTextBuilder.append("  ").append(lines[i]);
      if (i < last) {
        newTextBuilder.append("\n");
      }
    }
    final var dummyYamlFile = YAMLElementGenerator.getInstance(scalar.getProject()).createDummyYamlWithText(newTextBuilder.toString());
    var newKeyValue = PsiTreeUtil.findChildOfType(dummyYamlFile, YAMLKeyValue.class);
    if (newKeyValue == null || newKeyValue.getValue() == null) {
      return;
    }
    superParent.replace(newKeyValue);
  }

  private void replaceSequenceItem(YAMLScalarText scalar, PsiElement superParent) {
    var newTextBuilder = new StringBuilder("|\n");
    var content = scalar.getText();

    String[] lines = content.split("\\R");
    int last = lines.length - 1;
    for (int i = 0; i < lines.length; i++) {
      // scalar.getTextValue() returns correct string without > but removes line breaks so use scalar.getText() instead and filter out > lines
      var spaceRemovedLine = lines[i].replaceFirst("^\\s+", "").trim();
      if (spaceRemovedLine.equals(">")) {
        continue;
      }
      newTextBuilder.append("  ").append(lines[i]);
      if (i < last) {
        newTextBuilder.append("\n");
      }
    }
    var newSequenceItem = YAMLElementGenerator.getInstance(scalar.getProject()).createSequenceItem(newTextBuilder.toString());
    if (newSequenceItem.getValue() == null) {
      return;
    }
    superParent.replace(newSequenceItem);
  }
}
