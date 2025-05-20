package com.github.deeepamin.ciaid.settings;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
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
import com.intellij.util.ui.JBFont;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
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
  private JTextArea defaultGitlabCIYamlPathCommentLabel;
  private JBCheckBox ignoreUndefinedJobCheckBox;
  private JTextArea ignoreUndefinedJobOrStageCommentLabel;
  private JBCheckBox ignoreUndefinedStageCheckBox;
  private JBCheckBox ignoreUndefinedScriptCheckBox;
  private JBCheckBox ignoreUndefinedIncludeCheckBox;
  private JBTable userMarkedFilesTable;
  private JPanel userMarkedFilesPanel;
  private final List<String> removedFiles = new ArrayList<>();

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return CIAidBundle.message("settings.display.name");
  }

  @Override
  public @Nullable JComponent createComponent() {
    configureDefaultYamlPathTextField();
    configureInspectionCheckboxes();
    configureUserMarkedFilesTable();

    return FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.general.separator")))
            .setFormLeftIndent(20)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.general.ci.yaml.path") + ":"), defaultGitlabCIYamlPathField)
            .addComponent(defaultGitlabCIYamlPathCommentLabel)
            .setFormLeftIndent(0)
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.inspections.separator")))
            .setFormLeftIndent(20)
            .addComponent(ignoreUndefinedJobCheckBox)
            .addComponent(ignoreUndefinedStageCheckBox)
            .addComponent(ignoreUndefinedJobOrStageCommentLabel)
            .addComponent(ignoreUndefinedScriptCheckBox)
            .addComponent(ignoreUndefinedIncludeCheckBox)
            .setFormLeftIndent(0)
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.user.yamls.separator")), 6)
            .setFormLeftIndent(20)
            .addComponent(userMarkedFilesPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  private void configureDefaultYamlPathTextField() {
    defaultGitlabCIYamlPathField = new JBTextField();
    defaultGitlabCIYamlPathField.getEmptyText().setText(CIAidBundle.message("settings.general.ci.yaml.path.empty-text"));
    defaultGitlabCIYamlPathCommentLabel = getCommentLabel(CIAidBundle.message("settings.general.ci.yaml.path.comment-text"));
    ComponentValidator validator = new ComponentValidator(() -> {})
            .withValidator(() -> {
              String path = defaultGitlabCIYamlPathField.getText();
              var projectBasePath = project.getBasePath();
              if (path.isBlank()) {
                return null;
              }
              var file = new File(path);
              if (!file.exists()) {
                file = new File(projectBasePath, path);
                if (!file.exists() || !(path.endsWith(".yml") || path.endsWith(".yaml"))) {
                  return new ValidationInfo(CIAidBundle.message("settings.general.ci.yaml.path.non-existing-file"), defaultGitlabCIYamlPathField);
                }
              } else {
                // file exists, check if it is in project
                assert projectBasePath != null;
                if (!path.contains(projectBasePath) || !(path.endsWith(".yml") || path.endsWith(".yaml"))) {
                  return new ValidationInfo(CIAidBundle.message("settings.general.ci.yaml.path.not-in-current-project"), defaultGitlabCIYamlPathField);
                }
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
    ignoreUndefinedJobCheckBox = new JBCheckBox(CIAidBundle.message("settings.inspections.ignore-undefined-job"));
    ignoreUndefinedStageCheckBox = new JBCheckBox(CIAidBundle.message("settings.inspections.ignore-undefined-stage"));
    ignoreUndefinedScriptCheckBox = new JBCheckBox(CIAidBundle.message("settings.inspections.ignore-script-unavailable"));
    ignoreUndefinedIncludeCheckBox = new JBCheckBox(CIAidBundle.message("settings.inspections.ignore-include-unavailable"));

    ignoreUndefinedJobCheckBox.setSelected(CIAidSettingsState.getInstance(project).ignoreUndefinedJob);
    ignoreUndefinedStageCheckBox.setSelected(CIAidSettingsState.getInstance(project).ignoreUndefinedStage);
    ignoreUndefinedJobOrStageCommentLabel = getCommentLabel(CIAidBundle.message("settings.inspections.ignore-undefined-job-stage-comment"));
    ignoreUndefinedScriptCheckBox.setSelected(CIAidSettingsState.getInstance(project).ignoreUndefinedScript);
    ignoreUndefinedIncludeCheckBox.setSelected(CIAidSettingsState.getInstance(project).ignoreUndefinedInclude);
  }

  private void configureUserMarkedFilesTable() {
    DefaultTableModel tableModel = new DefaultTableModel(new Object[]{CIAidBundle.message("settings.user.yamls.table.path-column"), CIAidBundle.message("settings.user.yamls.table.ignore-column")}, 0) {
    };

    userMarkedFilesTable = new JBTable(tableModel);
    userMarkedFilesTable.setRowHeight(28);
    userMarkedFilesTable.getEmptyText().setText(CIAidBundle.message("settings.user.yamls.table.empty-text"));
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

    var ignoreColumn = userMarkedFilesTable.getColumnModel().getColumn(1);
    ignoreColumn.setCellEditor(new DefaultCellEditor(new JBCheckBox()));
    ignoreColumn.setCellRenderer(userMarkedFilesTable.getDefaultRenderer(Boolean.class));
    var ignoreHeaderRenderer = ignoreColumn.getHeaderRenderer();
    if (ignoreHeaderRenderer == null) {
      ignoreHeaderRenderer = userMarkedFilesTable.getTableHeader().getDefaultRenderer();
    }
    var ignoreHeaderComp = ignoreHeaderRenderer.getTableCellRendererComponent(userMarkedFilesTable, ignoreColumn.getHeaderValue(), false, false, -1, 1);
    int preferredWidth = ignoreHeaderComp.getPreferredSize().width + 10; // +10 for padding around ignore
    ignoreColumn.setMaxWidth(preferredWidth);
    ignoreColumn.setMinWidth(preferredWidth);
    ignoreColumn.setPreferredWidth(preferredWidth);

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

  private JTextArea getCommentLabel(String comment) {
    JTextArea textArea = new JTextArea(comment);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setFocusable(false);
    textArea.setOpaque(false);
    textArea.setFont(JBFont.medium());
    textArea.setForeground(UIManager.getColor("Label.disabledForeground"));
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
    return textArea;
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
        var projectService = CIAidProjectService.getInstance(project);
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
    var projectService = CIAidProjectService.getInstance(project);
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
