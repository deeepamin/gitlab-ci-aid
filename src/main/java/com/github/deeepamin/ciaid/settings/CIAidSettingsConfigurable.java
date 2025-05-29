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
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
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
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
  private JBTextField gitlabServerTextField;
  private JPanel gitlabServerPanel;
  private JBPasswordField gitlabAccessTokenField;
  private JPanel gitlabAccessTokenPanel;
  private JBTextField gitlabTemplatesProjectField;
  private JPanel gitlabTemplatesProjectPanel;
  private JBTextField gitlabTemplatesPathField;
  private JPanel gitlabTemplatesPathPanel;
  private JBCheckBox ignoreUndefinedJobCheckBox;
  private JBTextArea ignoreUndefinedJobOrStageCommentLabel;
  private JBCheckBox ignoreUndefinedStageCheckBox;
  private JBCheckBox ignoreUndefinedScriptCheckBox;
  private JBCheckBox ignoreUndefinedIncludeCheckBox;
  private JBTable userMarkedFilesTable;
  private JBScrollPane userMarkedFilesScrollPane;

  private final List<String> removedFiles = new ArrayList<>();

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return CIAidBundle.message("settings.display.name");
  }

  @Override
  public @Nullable JComponent createComponent() {
    configureDefaultYamlPathTextField();
    configureGitLabSettings();
    configureInspectionCheckboxes();
    configureUserMarkedFilesTable();

    return FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.general.separator")))
            .setFormLeftIndent(20)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.general.ci.yaml.path") + ":"), defaultGitlabCIYamlPathFieldWithHelpPanel)
            .setFormLeftIndent(0)
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.gitlab.separator")))
            .setFormLeftIndent(20)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.server") + ":"), gitlabServerPanel)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.token") + ":"), gitlabAccessTokenPanel)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.templates.project") + ":"), gitlabTemplatesProjectPanel)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.templates.path") + ":"), gitlabTemplatesPathPanel)
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


  private void configureGitLabSettings() {
    gitlabServerTextField = new JBTextField();
    //noinspection DialogTitleCapitalization
    gitlabServerTextField.getEmptyText().setText(CIAidBundle.message("settings.gitlab.server.empty-text"));
    var helpLabel = ContextHelpLabel.create(CIAidBundle.message("settings.gitlab.server.comment-text"));

    gitlabServerPanel = new JPanel(new BorderLayout(5, 0));
    gitlabServerPanel.add(gitlabServerTextField, BorderLayout.CENTER);
    gitlabServerPanel.add(helpLabel, BorderLayout.EAST);

    gitlabAccessTokenField = new JBPasswordField();
    //noinspection DialogTitleCapitalization
    gitlabAccessTokenField.getEmptyText().setText(CIAidBundle.message("settings.gitlab.token.empty-text"));
    helpLabel = ContextHelpLabel.create(CIAidBundle.message("settings.gitlab.token.comment-text"));

    gitlabAccessTokenPanel = new JPanel(new BorderLayout(5, 0));
    gitlabAccessTokenPanel.add(gitlabAccessTokenField, BorderLayout.CENTER);
    gitlabAccessTokenPanel.add(helpLabel, BorderLayout.EAST);

    gitlabTemplatesProjectField = new JBTextField();
    //noinspection DialogTitleCapitalization
    gitlabTemplatesProjectField.getEmptyText().setText(CIAidBundle.message("settings.gitlab.templates.project.empty-text"));
    helpLabel = ContextHelpLabel.create(CIAidBundle.message("settings.gitlab.templates.project.comment-text"));


    gitlabTemplatesProjectPanel = new JPanel(new BorderLayout(5, 0));
    gitlabTemplatesProjectPanel.add(gitlabTemplatesProjectField, BorderLayout.CENTER);
    gitlabTemplatesProjectPanel.add(helpLabel, BorderLayout.EAST);

    gitlabTemplatesPathField = new JBTextField();
    //noinspection DialogTitleCapitalization
    gitlabTemplatesPathField.getEmptyText().setText(CIAidBundle.message("settings.gitlab.templates.path.empty-text"));
    helpLabel = ContextHelpLabel.create(CIAidBundle.message("settings.gitlab.templates.path.comment-text"));

    gitlabTemplatesPathPanel = new JPanel(new BorderLayout(5, 0));
    gitlabTemplatesPathPanel.add(gitlabTemplatesPathField, BorderLayout.CENTER);
    gitlabTemplatesPathPanel.add(helpLabel, BorderLayout.EAST);
  }


  private void configureInspectionCheckboxes() {
    ignoreUndefinedJobCheckBox = new JBCheckBox(CIAidBundle.message("settings.inspections.ignore-undefined-job"));
    ignoreUndefinedStageCheckBox = new JBCheckBox(CIAidBundle.message("settings.inspections.ignore-undefined-stage"));
    ignoreUndefinedScriptCheckBox = new JBCheckBox(CIAidBundle.message("settings.inspections.ignore-script-unavailable"));
    ignoreUndefinedIncludeCheckBox = new JBCheckBox(CIAidBundle.message("settings.inspections.ignore-include-unavailable"));

    ignoreUndefinedJobCheckBox.setSelected(CIAidSettingsState.getInstance(project).isIgnoreUndefinedJob());
    ignoreUndefinedStageCheckBox.setSelected(CIAidSettingsState.getInstance(project).isIgnoreUndefinedStage());
    ignoreUndefinedJobOrStageCommentLabel = getCommentLabel(CIAidBundle.message("settings.inspections.ignore-undefined-job-stage-comment"));
    ignoreUndefinedScriptCheckBox.setSelected(CIAidSettingsState.getInstance(project).isIgnoreUndefinedScript());
    ignoreUndefinedIncludeCheckBox.setSelected(CIAidSettingsState.getInstance(project).isIgnoreUndefinedInclude());
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

  private JBTextArea getCommentLabel(String comment) {
    JBTextArea textArea = new JBTextArea(comment) {
      @Override
      public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        int width = getParent() != null ? getParent().getWidth() - 40 : 400;
        return new Dimension(Math.min(width, size.width), size.height);
      }
    };
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
    virtualFile.refresh(true, false);
  }

  @Override
  public boolean isModified() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);

    return !defaultGitlabCIYamlPathField.getText().equals(ciaidSettingsState.getDefaultGitlabCIYamlPath())
            || !gitlabServerTextField.getText().trim().equals(ciaidSettingsState.getGitlabServerUrl())
            || !gitlabTemplatesProjectField.getText().trim().equals(ciaidSettingsState.getGitlabTemplatesProject())
            || !new String(gitlabAccessTokenField.getPassword()).trim().equals(ciaidSettingsState.getCachedGitLabAccessToken())
            || !gitlabTemplatesPathField.getText().trim().equals(ciaidSettingsState.getGitlabTemplatesPath())
            || ignoreUndefinedJobCheckBox.isSelected() != ciaidSettingsState.isIgnoreUndefinedJob()
            || ignoreUndefinedStageCheckBox.isSelected() != ciaidSettingsState.isIgnoreUndefinedStage()
            || ignoreUndefinedScriptCheckBox.isSelected() != ciaidSettingsState.isIgnoreUndefinedScript()
            || ignoreUndefinedIncludeCheckBox.isSelected() != ciaidSettingsState.isIgnoreUndefinedInclude()
            || !getYamlToUserMarkings().equals(ciaidSettingsState.getYamlToUserMarkings());
  }

  @Override
  public void apply() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    ciaidSettingsState.setDefaultGitlabCIYamlPath(defaultGitlabCIYamlPathField.getText());
    ciaidSettingsState.setGitlabServerUrl(gitlabServerTextField.getText().trim());
    ciaidSettingsState.saveGitLabAccessToken(gitlabServerTextField.getText().trim(), new String(gitlabAccessTokenField.getPassword()).trim());
    ciaidSettingsState.setGitlabTemplatesProject(gitlabTemplatesProjectField.getText().trim());
    ciaidSettingsState.setGitlabTemplatesPath(gitlabTemplatesPathField.getText().trim());
    ciaidSettingsState.setIgnoreUndefinedJob(ignoreUndefinedJobCheckBox.isSelected());
    ciaidSettingsState.setIgnoreUndefinedStage(ignoreUndefinedStageCheckBox.isSelected());
    ciaidSettingsState.setIgnoreUndefinedScript(ignoreUndefinedScriptCheckBox.isSelected());
    ciaidSettingsState.setIgnoreUndefinedInclude(ignoreUndefinedIncludeCheckBox.isSelected());

    PsiManager.getInstance(project).dropPsiCaches();
    removedFiles.forEach(path -> {
      var virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (virtualFile != null) {
        var projectService = CIAidProjectService.getInstance(project);
        GitlabCIYamlUtils.removeMarkingOfUserCIYamlFile(virtualFile);
        projectService.getPluginData().remove(virtualFile);
        refreshVirtualFile(virtualFile);
      }
      ciaidSettingsState.getYamlToUserMarkings().remove(path);
    });
    removedFiles.clear();

    getYamlToUserMarkings().forEach((path, markOrIgnore) -> {
      var virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (virtualFile == null) {
        return;
      }
      if (ciaidSettingsState.getYamlToUserMarkings().containsKey(path)) {
        var markOrIgnoreFromState = ciaidSettingsState.getYamlToUserMarkings().get(path);
        if (markOrIgnore != markOrIgnoreFromState) {
          handleFile(virtualFile, markOrIgnore);
        }
      } else {
        handleFile(virtualFile, markOrIgnore);
      }
    });
    ciaidSettingsState.setYamlToUserMarkings(getYamlToUserMarkings());
  }

  @Override
  public void reset() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    defaultGitlabCIYamlPathField.setText(ciaidSettingsState.getDefaultGitlabCIYamlPath());
    gitlabServerTextField.setText(ciaidSettingsState.getGitlabServerUrl());
    gitlabAccessTokenField.setText(ciaidSettingsState.getGitLabAccessToken());
    gitlabTemplatesProjectField.setText(ciaidSettingsState.getGitlabTemplatesProject());
    gitlabTemplatesPathField.setText(ciaidSettingsState.getGitlabTemplatesPath());
    ignoreUndefinedJobCheckBox.setSelected(ciaidSettingsState.isIgnoreUndefinedJob());
    ignoreUndefinedStageCheckBox.setSelected(ciaidSettingsState.isIgnoreUndefinedStage());
    ignoreUndefinedScriptCheckBox.setSelected(ciaidSettingsState.isIgnoreUndefinedScript());
    ignoreUndefinedIncludeCheckBox.setSelected(ciaidSettingsState.isIgnoreUndefinedInclude());
    DefaultTableModel tableModel = (DefaultTableModel) userMarkedFilesTable.getModel();
    tableModel.setRowCount(0);
    removedFiles.clear();
    ciaidSettingsState.getYamlToUserMarkings().forEach((path, markOrIgnore) -> tableModel.addRow(new Object[]{path, markOrIgnore}));
  }

  @Override
  public void disposeUIResources() {
    defaultGitlabCIYamlPathField = null;
    defaultGitlabCIYamlPathFieldWithHelpPanel = null;
    gitlabServerTextField = null;
    gitlabServerPanel = null;
    gitlabAccessTokenField = null;
    gitlabAccessTokenPanel = null;
    gitlabTemplatesProjectField = null;
    gitlabTemplatesProjectPanel = null;
    gitlabTemplatesPathField = null;
    gitlabTemplatesPathPanel = null;
    ignoreUndefinedJobCheckBox = null;
    ignoreUndefinedJobOrStageCommentLabel = null;
    ignoreUndefinedStageCheckBox = null;
    ignoreUndefinedScriptCheckBox = null;
    ignoreUndefinedIncludeCheckBox = null;
    userMarkedFilesTable = null;
    userMarkedFilesScrollPane = null;
  }
}
