package com.github.deeepamin.ciaid.refactor.dialogs;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

public class ExtractReferenceDialog extends BaseRefactorToFileDialog {
  private JBTextField referenceTextField;
  private JPanel referenceAndTextFieldPanel;

  public ExtractReferenceDialog(Project project, String keyName, List<VirtualFile> files) {
    super(project, keyName, files);
  }

  @Override
  protected String getRefactoringType() {
    return CIAidBundle.message("refactoring.extract.reference.dialog.title");
  }

  @Override
  protected void configureDialogContents() {
    JLabel referenceLabel = new JLabel(CIAidBundle.message("refactoring.extract.reference.text") + ": ");
    referenceTextField = new JBTextField();
    referenceTextField.setText(keyName.startsWith(".") ? keyName : "." + keyName);
    referenceAndTextFieldPanel = new JPanel(new BorderLayout());
    referenceAndTextFieldPanel.add(referenceLabel, BorderLayout.WEST);
    referenceAndTextFieldPanel.add(referenceTextField, BorderLayout.CENTER);
    super.configureDialogContents();
  }

  @Override
  protected void customizeFormBuilder(FormBuilder formBuilder) {
    super.customizeFormBuilder(formBuilder);
    formBuilder.addComponent(referenceAndTextFieldPanel);
  }

  public String getReferenceKey() {
    return referenceTextField.getText() == null ? "" : referenceTextField.getText().trim();
  }
}
