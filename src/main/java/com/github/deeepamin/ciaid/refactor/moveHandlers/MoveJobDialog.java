package com.github.deeepamin.ciaid.refactor.moveHandlers;

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

public class MoveJobDialog extends DialogWrapper {
  private final Project project;
  private final String jobName;
  private final List<VirtualFile> filteredFiles;
  private boolean showOpenFileInEditor;
  private String selectedFilePath;

  private JBLabel moveJobLabel;
  private JPanel moveJobLabelAndComboBoxPanel;
  private ComboBox<String> filePathComboBox;
  private JBCheckBox openFileCheckBox;

  public MoveJobDialog(Project project, String jobName, List<VirtualFile> filteredFiles) {
    super(project);
    this.project = project;
    this.jobName = jobName;
    this.filteredFiles = filteredFiles;
    init();
    setSize(600, 150);
    setOKButtonText("Refactor");
    setTitle("Move"); //TODO
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    configureDialogContents();
    return FormBuilder.createFormBuilder()
            .addComponent(moveJobLabel)
            .addComponent(moveJobLabelAndComboBoxPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  @Override
  protected JComponent createSouthPanel() {
    JComponent defaultButtons = super.createSouthPanel();

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(openFileCheckBox, BorderLayout.WEST);
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

  private void configureDialogContents() {
    moveJobLabel = new JBLabel("Move " + jobName);
    moveJobLabel.setFont(moveJobLabel.getFont().deriveFont(Font.BOLD));
    moveJobLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    moveJobLabelAndComboBoxPanel = new JPanel(new BorderLayout());
    var toFileLabel = new JBLabel("To file" + ": "); //TODO
    filePathComboBox = new ComboBox<>();
    filteredFiles.forEach(file -> {
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
    moveJobLabelAndComboBoxPanel.add(toFileLabel, BorderLayout.WEST);
    moveJobLabelAndComboBoxPanel.add(filePathComboBox, BorderLayout.CENTER);
    openFileCheckBox = new JBCheckBox("Open in Editor");
  }
}
