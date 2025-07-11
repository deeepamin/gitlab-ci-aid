package com.github.deeepamin.ciaid.refactor.dialogs;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

public abstract class BaseRefactorToFileDialog extends DialogWrapper {
  protected final Project project;
  protected final String keyName;
  protected final List<VirtualFile> files;
  private boolean showOpenFileInEditor;
  private String selectedFilePath;

  private JBLabel refactorKeyLabel;
  private JPanel toFileLabelAndComboBoxPanel;
  private ComboBox<String> filePathComboBox;
  private JBCheckBox openFileCheckBox;

  protected BaseRefactorToFileDialog(Project project, String keyName, List<VirtualFile> files) {
    super(project);
    this.project = project;
    this.keyName = keyName;
    this.files = files;
    init();
    setSize(600, 150);
    setOKButtonText(CIAidBundle.message("refactoring.dialog.ok.text"));
    setTitle(getRefactoringType());
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return getCenterPanel();
  }

  private @Nullable JComponent getCenterPanel() {
    configureDialogContents();
    var formBuilder = FormBuilder.createFormBuilder();
    customizeFormBuilder(formBuilder);
    return formBuilder.addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  protected void customizeFormBuilder(FormBuilder formBuilder) {
    formBuilder.addComponent(refactorKeyLabel)
            .addComponent(toFileLabelAndComboBoxPanel);
  }

  @Override
  protected JComponent createSouthPanel() {
    JComponent defaultButtons = super.createSouthPanel();

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(openFileCheckBox, BorderLayout.CENTER);
    panel.add(defaultButtons, BorderLayout.EAST);
    panel.setBorder(defaultButtons.getBorder());

    return panel;
  }

  @Override
  protected void doOKAction() {
    var selectedItem = filePathComboBox.getSelectedItem();
    if (selectedItem != null) {
      this.selectedFilePath = filePathComboBox.getSelectedItem().toString();
    }
    this.showOpenFileInEditor = openFileCheckBox.isSelected();
    super.doOKAction();
  }

  public boolean shouldOpenFileInEditor() {
    return showOpenFileInEditor;
  }

  public String getSelectedFilePath() {
    return selectedFilePath;
  }

  protected abstract String getRefactoringType();

  protected void configureDialogContents() {
    refactorKeyLabel = new JBLabel(getRefactoringType() + " " + keyName);
    refactorKeyLabel.setFont(refactorKeyLabel.getFont().deriveFont(Font.BOLD));
    refactorKeyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    toFileLabelAndComboBoxPanel = new JPanel(new BorderLayout());
    var toFileLabel = new JBLabel(CIAidBundle.message("refactoring.dialog.to-file.text") + ": ");
    filePathComboBox = new ComboBox<>();
    files.forEach(file -> {
      var basePath = project.getBasePath();
      var filePath = file.getPath();
      String comboBoxPath = filePath;
      if (basePath != null && filePath.startsWith(basePath)) {
        comboBoxPath = filePath.substring(basePath.length() + 1);
      }
      var cacheDirPath = CIAidCacheService.getCiAidCacheDir().getPath();
      if (!filePath.startsWith(cacheDirPath)) {
        filePathComboBox.addItem(comboBoxPath);
      }
    });
    toFileLabelAndComboBoxPanel.add(toFileLabel, BorderLayout.WEST);
    toFileLabelAndComboBoxPanel.add(filePathComboBox, BorderLayout.CENTER);
    openFileCheckBox = new JBCheckBox(CIAidBundle.message("refactoring.dialog.open.in.editor"));
  }
}
