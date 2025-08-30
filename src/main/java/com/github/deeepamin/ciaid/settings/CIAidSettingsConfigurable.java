package com.github.deeepamin.ciaid.settings;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
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
  private JPanel defaultGitlabCIYamlPathFieldWithHelpPanel;
  private JBTable userMarkedFilesTable;
  private JBCheckBox editorNotificationDisabledCheckBox;
  private JBScrollPane userMarkedFilesScrollPane;

  private final List<String> removedFiles = new ArrayList<>();

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return CIAidBundle.message("settings.display.name");
  }

  @Override
  public @Nullable JComponent createComponent() {
    configureDefaultYamlPathTextField();
    configureUserMarkedFiles();

    //noinspection DialogTitleCapitalization
    return FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.general.separator")))
            .setFormLeftIndent(20)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.general.ci.yaml.path") + ":"), defaultGitlabCIYamlPathFieldWithHelpPanel)
            .setFormLeftIndent(0)
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.user.yamls.separator")), 6)
            .setFormLeftIndent(20)
            .addComponent(editorNotificationDisabledCheckBox)
            .addComponent(userMarkedFilesScrollPane)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  private void configureDefaultYamlPathTextField() {
    defaultGitlabCIYamlPathField = new JBTextField();
    defaultGitlabCIYamlPathField.getEmptyText().setText(CIAidBundle.message("settings.general.ci.yaml.path.empty-text"));
    var helpLabel = ContextHelpLabel.create(CIAidBundle.message("settings.general.ci.yaml.path.comment-text"));

    defaultGitlabCIYamlPathFieldWithHelpPanel = new JPanel(new BorderLayout(5, 0));
    defaultGitlabCIYamlPathFieldWithHelpPanel.add(defaultGitlabCIYamlPathField, BorderLayout.CENTER);
    defaultGitlabCIYamlPathFieldWithHelpPanel.add(helpLabel, BorderLayout.EAST);
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

  private void configureUserMarkedFiles() {
    editorNotificationDisabledCheckBox = new JBCheckBox(CIAidBundle.message("settings.user.yamls.editor.notification.disabled"));
    editorNotificationDisabledCheckBox.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    editorNotificationDisabledCheckBox.setSelected(CIAidSettingsState.getInstance(project).isEditorNotificationDisabled());

    DefaultTableModel tableModel = new DefaultTableModel(new Object[]{CIAidBundle.message("settings.user.yamls.table.path-or-glob-column"), CIAidBundle.message("settings.user.yamls.table.ignore-column")}, 0) {
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

    userMarkedFilesScrollPane = new JBScrollPane(ToolbarDecorator.createDecorator(userMarkedFilesTable)
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
            .createPanel());
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

  private void refreshVirtualFile(VirtualFile virtualFile) {
    virtualFile.refresh(true, false);
  }

  @Override
  public boolean isModified() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);

    return !defaultGitlabCIYamlPathField.getText().equals(ciaidSettingsState.getDefaultGitlabCIYamlPath())
            || editorNotificationDisabledCheckBox.isSelected() != ciaidSettingsState.isEditorNotificationDisabled()
            || !getYamlToUserMarkings().equals(ciaidSettingsState.getYamlToUserMarkings());
  }

  @Override
  public void apply() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    ciaidSettingsState.setDefaultGitlabCIYamlPath(defaultGitlabCIYamlPathField.getText());
    ciaidSettingsState.setEditorNotificationDisabled(editorNotificationDisabledCheckBox.isSelected());

    PsiManager.getInstance(project).dropPsiCaches();
    removedFiles.forEach(path -> {
      var pathContainsWildcard = CIAidUtils.containsWildcard(path);
      if (pathContainsWildcard) {
        var matchingFiles = FileUtils.findVirtualFilesByGlob(path, project);
        matchingFiles.forEach(this::doRemoveVirtualFile);
      } else {
        var virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (virtualFile != null) {
          doRemoveVirtualFile(virtualFile);
        }
      }
      ciaidSettingsState.getYamlToUserMarkings().remove(path);
    });
    removedFiles.clear();

    getYamlToUserMarkings().forEach((path, markOrIgnore) -> {
      var pathContainsWildcard = CIAidUtils.containsWildcard(path);
      if (pathContainsWildcard) {
        var matchingFiles = FileUtils.findVirtualFilesByGlob(path, project);
        matchingFiles.forEach(file -> doReadVirtualFile(path, markOrIgnore != null ? markOrIgnore : false, file));
      } else {
        var virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (virtualFile != null) {
          doReadVirtualFile(path, markOrIgnore != null ? markOrIgnore : false, virtualFile);
        }
      }
    });
    ciaidSettingsState.setYamlToUserMarkings(getYamlToUserMarkings());
  }

  private void doReadVirtualFile(String path, Boolean markOrIgnore, VirtualFile virtualFile) {
    var ciAidSettingsState = CIAidSettingsState.getInstance(project);

    if (ciAidSettingsState.getYamlToUserMarkings().containsKey(path)) {
      var markOrIgnoreFromState = ciAidSettingsState.getYamlToUserMarkings().get(path);
      if (markOrIgnore.equals(markOrIgnoreFromState)) {
        return;
      }
    }
    var projectService = CIAidProjectService.getInstance(project);
    if (markOrIgnore) {
      CIAidProjectService.ignoreCIYamlFile(virtualFile, project);
      projectService.getPluginData().remove(virtualFile);
    } else {
      CIAidProjectService.markAsUserCIYamlFile(virtualFile, project);
      projectService.readGitlabCIYamlData(virtualFile, true, false);
      EditorNotifications.getInstance(project).updateNotifications(virtualFile);
    }
    refreshVirtualFile(virtualFile);
  }

  private void doRemoveVirtualFile(VirtualFile virtualFile) {
    var projectService = CIAidProjectService.getInstance(project);
    CIAidProjectService.removeMarkingOfUserCIYamlFile(virtualFile);
    projectService.getPluginData().remove(virtualFile);
    refreshVirtualFile(virtualFile);
  }

  @Override
  public void reset() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    defaultGitlabCIYamlPathField.setText(ciaidSettingsState.getDefaultGitlabCIYamlPath());
    editorNotificationDisabledCheckBox.setSelected(ciaidSettingsState.isEditorNotificationDisabled());
    DefaultTableModel tableModel = (DefaultTableModel) userMarkedFilesTable.getModel();
    tableModel.setRowCount(0);
    removedFiles.clear();
    ciaidSettingsState.getYamlToUserMarkings().forEach((path, markOrIgnore) -> tableModel.addRow(new Object[]{path, markOrIgnore}));
  }

  @Override
  public void disposeUIResources() {
    defaultGitlabCIYamlPathField = null;
    defaultGitlabCIYamlPathFieldWithHelpPanel = null;
    userMarkedFilesTable = null;
    userMarkedFilesScrollPane = null;
  }
}
