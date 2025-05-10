package com.github.deeepamin.ciaid.settings;

import com.github.deeepamin.ciaid.GitlabCIAidBundle;
import com.github.deeepamin.ciaid.services.GitlabCIYamlProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CIAidSettingsConfigurable implements Configurable {
  private final Project project;

  public CIAidSettingsConfigurable(Project project) {
    this.project = project;
  }

  private JBTextField defaultGitlabCIYamlPathField;
  private JLabel defaultGitlabCIYamlPathCommentLabel;
  private JBCheckBox ignoreUndefinedJobCheckBox;
  private JLabel ignoreUndefinedJobOrStageCommentLabel;
  private JBCheckBox ignoreUndefinedStageCheckBox;
  private JBCheckBox ignoreUndefinedScriptCheckBox;
  private JBCheckBox ignoreUndefinedIncludeCheckBox;
  private JBTable userMarkedFilesTable;
  private JPanel userMarkedFilesPanel;
  private final List<String> removedFiles = new ArrayList<>();

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return GitlabCIAidBundle.message("configurable.ciaid.gitlab.display.name");
  }

  @Override
  public @Nullable JComponent createComponent() {
    configureDefaultYamlPathTextField();
    configureInspectionCheckboxes();
    configureUserMarkedFilesTable();

    return FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator("General Settings"))
            .setFormLeftIndent(20)
            .addLabeledComponent(new JLabel("GitLab CI Yaml Path:"), defaultGitlabCIYamlPathField)
            .addComponent(defaultGitlabCIYamlPathCommentLabel)
            .setFormLeftIndent(0)
            .addComponent(new TitledSeparator("Inspections"))
            .setFormLeftIndent(20)
            .addComponent(ignoreUndefinedJobCheckBox)
            .addComponent(ignoreUndefinedStageCheckBox)
            .addComponent(ignoreUndefinedJobOrStageCommentLabel)
            .addComponent(ignoreUndefinedScriptCheckBox)
            .addComponent(ignoreUndefinedIncludeCheckBox)
            .setFormLeftIndent(0)
            .addComponent(new TitledSeparator("User Marked Yamls"), 6)
            .setFormLeftIndent(20)
            .addComponent(userMarkedFilesPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  private void configureDefaultYamlPathTextField() {
    defaultGitlabCIYamlPathField = new JBTextField();
    defaultGitlabCIYamlPathField.getEmptyText().setText(".gitlab-ci.yml");
    defaultGitlabCIYamlPathCommentLabel = getCommentLabel("If project root directory doesn't contain .gitlab-ci.yml or .gitlab-ci.yaml, specify the path to default GitLab CI yaml relative to project root");
    ComponentValidator validator = new ComponentValidator(() -> {})
            .withValidator(() -> {
              String path = defaultGitlabCIYamlPathField.getText();
              if (path.isBlank()) {
                return null;
              }
              String basePath = project.getBasePath();
              if (path.startsWith(basePath + File.separator)) {
                path = path.replace(basePath + File.separator, "");
              }

              File file = new File(basePath, path);
              if (!file.exists() || !(path.endsWith(".yml") || path.endsWith(".yaml"))) {
                return new ValidationInfo("File must be in opened project and end with .yml or .yaml", defaultGitlabCIYamlPathField);
              }
              return null;
            })
            .installOn(defaultGitlabCIYamlPathField);

    defaultGitlabCIYamlPathField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        validator.revalidate();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        validator.revalidate();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        validator.revalidate();
      }
    });
  }

  private void configureInspectionCheckboxes() {
    ignoreUndefinedJobCheckBox = new JBCheckBox("Ignore \"Undefined Job\" errors");
    ignoreUndefinedStageCheckBox = new JBCheckBox("Ignore \"Undefined Stage\" errors");
    ignoreUndefinedScriptCheckBox = new JBCheckBox("Ignore \"Script not available on path\" errors");
    ignoreUndefinedIncludeCheckBox = new JBCheckBox("Ignore \"Include file not available on path\" errors");

    ignoreUndefinedJobCheckBox.setSelected(CIAidSettingsState.getInstance(project).ignoreUndefinedJob);
    ignoreUndefinedStageCheckBox.setSelected(CIAidSettingsState.getInstance(project).ignoreUndefinedStage);
    ignoreUndefinedJobOrStageCommentLabel = getCommentLabel("If components, remote files or templates are used and not defined in the current file, ignore job and stage errors");
    ignoreUndefinedScriptCheckBox.setSelected(CIAidSettingsState.getInstance(project).ignoreUndefinedScript);
    ignoreUndefinedIncludeCheckBox.setSelected(CIAidSettingsState.getInstance(project).ignoreUndefinedInclude);
  }

  private void configureUserMarkedFilesTable() {
    DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Path", "Ignore"}, 0) {
    };

    userMarkedFilesTable = new JBTable(tableModel);
    userMarkedFilesTable.setRowHeight(28);
    userMarkedFilesTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
    userMarkedFilesTable.getEmptyText().setText("No files added. Click '+' to add a file path.");
    userMarkedFilesTable.getColumnModel().getColumn(0).setCellEditor(new CIAidUserMarkedFilesTablePathEditor(project));
    userMarkedFilesTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBorder(BorderFactory.createEmptyBorder());
        setText(value != null ? value.toString() : "");
        return this;
      }
    });
    userMarkedFilesTable.getColumnModel().getColumn(0).setPreferredWidth(470);

    userMarkedFilesTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JBCheckBox()));
    userMarkedFilesTable.getColumnModel().getColumn(1).setCellRenderer(userMarkedFilesTable.getDefaultRenderer(Boolean.class));

    userMarkedFilesPanel = ToolbarDecorator.createDecorator(userMarkedFilesTable)
            .setAddAction(button -> tableModel.addRow(new Object[]{"", false}))
            .setRemoveAction(button -> {
              int selectedRow = userMarkedFilesTable.getSelectedRow();
              if (selectedRow != -1) {
                var path = (String) tableModel.getValueAt(selectedRow, 0);
                removedFiles.add(path);
                tableModel.removeRow(selectedRow);
              }
            })
            .disableUpDownActions()
            .createPanel();

  }

  private JLabel getCommentLabel(String comment) {
    JLabel commentLabel = new JLabel(comment);
    commentLabel.setFont(commentLabel.getFont().deriveFont(Font.PLAIN, 11f));
    commentLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
    commentLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
    return commentLabel;
  }

  private Map<String, Boolean> getYamlToUserMarkings() {
    DefaultTableModel tableModel = (DefaultTableModel) userMarkedFilesTable.getModel();
    Map<String, Boolean> yamlToUserMarkings = new HashMap<>();
    for (int i = 0; i < tableModel.getRowCount(); i++) {
      String path = (String) tableModel.getValueAt(i, 0);
      boolean markOrIgnore = tableModel.getValueAt(i, 1) != null && (boolean) tableModel.getValueAt(i, 1);
      if (path != null && !path.isEmpty()) {
        yamlToUserMarkings.put(path.trim(), markOrIgnore);
      }
    }
    return yamlToUserMarkings;
  }

  @Override
  public boolean isModified() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);

    return !defaultGitlabCIYamlPathField.getText().equals(ciaidSettingsState.defaultGitlabCIYamlPath)
            || ignoreUndefinedJobCheckBox.isSelected() != ciaidSettingsState.ignoreUndefinedJob
            || ignoreUndefinedStageCheckBox.isSelected() != ciaidSettingsState.ignoreUndefinedStage
            || ignoreUndefinedScriptCheckBox.isSelected() != ciaidSettingsState.ignoreUndefinedScript
            || ignoreUndefinedIncludeCheckBox.isSelected() != ciaidSettingsState.ignoreUndefinedInclude
            || !getYamlToUserMarkings().equals(ciaidSettingsState.yamlToUserMarkings);
  }

  @Override
  public void apply() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    ciaidSettingsState.defaultGitlabCIYamlPath = defaultGitlabCIYamlPathField.getText();
    ciaidSettingsState.ignoreUndefinedJob = ignoreUndefinedJobCheckBox.isSelected();
    ciaidSettingsState.ignoreUndefinedStage = ignoreUndefinedStageCheckBox.isSelected();
    ciaidSettingsState.ignoreUndefinedScript = ignoreUndefinedScriptCheckBox.isSelected();
    ciaidSettingsState.ignoreUndefinedInclude = ignoreUndefinedIncludeCheckBox.isSelected();

    removedFiles.forEach(path -> {
      var virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (virtualFile != null) {
        var projectService = GitlabCIYamlProjectService.getInstance(project);
        GitlabCIYamlUtils.removeMarkingOfUserCIYamlFile(virtualFile);
        projectService.getPluginData().remove(virtualFile);
        refreshVirtualFile(virtualFile);
      }
      ciaidSettingsState.yamlToUserMarkings.remove(path);
    });
    removedFiles.clear();

    getYamlToUserMarkings().forEach((path, markOrIgnore) -> {
      var virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (virtualFile == null) {
        return;
      }
      if (ciaidSettingsState.yamlToUserMarkings.containsKey(path)) {
        var markOrIgnoreFromState = ciaidSettingsState.yamlToUserMarkings.get(path);
        if (markOrIgnore != markOrIgnoreFromState) {
          handleFile(virtualFile, markOrIgnore);
        }
      } else {
        handleFile(virtualFile, markOrIgnore);
      }
    });
    ciaidSettingsState.yamlToUserMarkings = getYamlToUserMarkings();
  }

  private void handleFile(VirtualFile virtualFile, boolean ignore) {
    var projectService = GitlabCIYamlProjectService.getInstance(project);
    if (ignore) {
      GitlabCIYamlUtils.ignoreCIYamlFile(virtualFile, project);
      projectService.getPluginData().remove(virtualFile);
    } else {
      GitlabCIYamlUtils.markAsUserCIYamlFile(virtualFile, project);
      projectService.readGitlabCIYamlData(project, virtualFile, true);
    }
    refreshVirtualFile(virtualFile);
  }

  private void refreshVirtualFile(VirtualFile virtualFile) {
    PsiManager.getInstance(project).dropPsiCaches();
    virtualFile.refresh(true, false);
  }

  @Override
  public void reset() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    defaultGitlabCIYamlPathField.setText(ciaidSettingsState.defaultGitlabCIYamlPath);
    ignoreUndefinedJobCheckBox.setSelected(ciaidSettingsState.ignoreUndefinedJob);
    ignoreUndefinedStageCheckBox.setSelected(ciaidSettingsState.ignoreUndefinedStage);
    ignoreUndefinedScriptCheckBox.setSelected(ciaidSettingsState.ignoreUndefinedScript);
    ignoreUndefinedIncludeCheckBox.setSelected(ciaidSettingsState.ignoreUndefinedInclude);
    DefaultTableModel tableModel = (DefaultTableModel) userMarkedFilesTable.getModel();
    tableModel.setRowCount(0);
    removedFiles.clear();
    ciaidSettingsState.yamlToUserMarkings.forEach((path, markOrIgnore) -> tableModel.addRow(new Object[]{path, markOrIgnore}));
  }

  @Override
  public void disposeUIResources() {
    defaultGitlabCIYamlPathField = null;
    defaultGitlabCIYamlPathCommentLabel = null;
    ignoreUndefinedJobCheckBox = null;
    ignoreUndefinedJobOrStageCommentLabel = null;
    ignoreUndefinedStageCheckBox = null;
    ignoreUndefinedScriptCheckBox = null;
    ignoreUndefinedIncludeCheckBox = null;
    userMarkedFilesTable = null;
    userMarkedFilesPanel = null;
  }
}
