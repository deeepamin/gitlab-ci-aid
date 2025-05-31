package com.github.deeepamin.ciaid.settings.remotes;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class CIAidRemotesConfigurable implements Configurable {
  private final Project project;

  private final List<Remote> remotes = new ArrayList<>();

  private JBTable remotesTable;
  private JBScrollPane remotesTableScrollPane;
  private JBTextField gitlabTemplatesProjectField;
  private JPanel gitlabTemplatesProjectPanel;
  private JBTextField gitlabTemplatesPathField;
  private JPanel gitlabTemplatesPathPanel;
  private JPanel cachingSettingsPanel;
  private JBCheckBox cachingEnabledCheckBox;
  private JLabel cacheExpiryTimeLabel;
  private JBTextField cacheExpiryTimeTextField;
  private JButton clearCacheButton;

  public CIAidRemotesConfigurable(Project project) {
    this.project = project;
  }

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return CIAidBundle.message("settings.remotes.display.name");
  }

  @Override
  public @Nullable JComponent createComponent() {
    configureRemotesTable();
    configureTemplatesSettings();
    configureCachingSettings();

    return FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.remotes.gitlab.project.config.separator")))
            .setFormLeftIndent(20)
            .addComponent(remotesTableScrollPane)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.templates.project") + ":"), gitlabTemplatesProjectPanel)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.templates.path") + ":"), gitlabTemplatesPathPanel)
            .setFormLeftIndent(0)
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.remotes.caching.separator")))
            .setFormLeftIndent(20)
            .addComponent(cachingSettingsPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  private void configureTemplatesSettings() {
    //noinspection DialogTitleCapitalization
    var gitlabTemplatesProjectFieldAndPanel = getTextFieldWithHelp(CIAidBundle.message("settings.gitlab.templates.project.empty-text"),
            CIAidBundle.message("settings.gitlab.templates.project.comment-text"));
    gitlabTemplatesProjectField = gitlabTemplatesProjectFieldAndPanel.first;
    gitlabTemplatesProjectPanel = gitlabTemplatesProjectFieldAndPanel.second;

    //noinspection DialogTitleCapitalization
    var gitlabTemplatesPathFieldAndPanel = getTextFieldWithHelp(CIAidBundle.message("settings.gitlab.templates.path.empty-text"),
            CIAidBundle.message("settings.gitlab.templates.path.comment-text"));
    gitlabTemplatesPathField = gitlabTemplatesPathFieldAndPanel.first;
    gitlabTemplatesPathPanel = gitlabTemplatesPathFieldAndPanel.second;
  }

  @Override
  public boolean isModified() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    return !ciaidSettingsState.getRemotes().equals(remotes)
            || ciaidSettingsState.getGitlabTemplatesProject() != null
                  && !ciaidSettingsState.getGitlabTemplatesProject().equals(gitlabTemplatesProjectField.getText().trim())
            || ciaidSettingsState.getGitlabTemplatesPath() != null
                  && !ciaidSettingsState.getGitlabTemplatesPath().equals(gitlabTemplatesPathField.getText().trim())
            || ciaidSettingsState.isCachingEnabled() != cachingEnabledCheckBox.isSelected()
            || !ciaidSettingsState.getCacheExpiryTime().equals(Long.valueOf(cacheExpiryTimeTextField.getText().trim()));
  }

  @Override
  public void apply() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    ciaidSettingsState.setRemotes(remotes);
    ciaidSettingsState.setGitlabTemplatesProject(gitlabTemplatesProjectField.getText().trim());
    ciaidSettingsState.setGitlabTemplatesPath(gitlabTemplatesPathField.getText().trim());
    ciaidSettingsState.setCachingEnabled(cachingEnabledCheckBox.isSelected());
    try {
      long cacheExpiryTime = Long.parseLong(cacheExpiryTimeTextField.getText().trim());
      ciaidSettingsState.setCacheExpiryTime(cacheExpiryTime);
    } catch (NumberFormatException e) {
      ciaidSettingsState.setCacheExpiryTime(CIAidSettingsState.getInstance(project).getCacheExpiryTime());
    }
  }

  @Override
  public void reset() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    remotes.clear();
    remotes.addAll(ciaidSettingsState.getRemotes());
    gitlabTemplatesProjectField.setText(ciaidSettingsState.getGitlabTemplatesProject());
    gitlabTemplatesPathField.setText(ciaidSettingsState.getGitlabTemplatesPath());

    cachingEnabledCheckBox.setSelected(ciaidSettingsState.isCachingEnabled());
    cacheExpiryTimeTextField.setText(String.valueOf(ciaidSettingsState.getCacheExpiryTime()));
  }

  private void configureRemotesTable() {
    var tableModel = getRemotesListTableModel();
    remotesTable = new JBTable(tableModel);
    remotesTable.setSelectionMode(SINGLE_SELECTION);
    remotesTable.getColumnModel().getColumn(2).setCellRenderer(new AccessTokenCellRenderer());
    remotesTableScrollPane = new JBScrollPane(ToolbarDecorator.createDecorator(remotesTable)
            .setAddAction(button -> {
              var remoteDialog = new RemoteDialog(project, false, null);
              if (remoteDialog.showAndGet()) {
                var remote = remoteDialog.getRemote();
                if (remote != null) {
                  tableModel.addRow(remote);
                }
              }
            })
            .setRemoveAction(button -> {
              int selectedRow = remotesTable.getSelectedRow();
              if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
              }
            })
            .setEditAction(button -> {
              int selectedRow = remotesTable.getSelectedRow();
              if (selectedRow == -1) {
                return;
              }
              var remote = tableModel.getItem(selectedRow);
              if (remote == null) {
                return;
              }
              var remoteDialog = new RemoteDialog(project, false, remote);
              if (remoteDialog.showAndGet()) {
                tableModel.addRow(remote);
              }
            })
            .disableUpDownActions()
            .createPanel());
  }

  private @NotNull ListTableModel<Remote> getRemotesListTableModel() {
    var columns = new ColumnInfo[]{
            new ColumnInfo<Remote, String>(CIAidBundle.message("settings.remotes.table.gitlab-api-url-column")) {
              @Override
              public String valueOf(Remote remote) {
                return remote.getApiUrl();
              }
            },
            new ColumnInfo<Remote, String>(CIAidBundle.message("settings.remotes.table.project-column")) {
              @Override
              public String valueOf(Remote remote) {
                return remote.getProject();
              }
            },
            new ColumnInfo<Remote, String>(CIAidBundle.message("settings.remotes.table.access-token-column")) {
              @Override
              public String valueOf(Remote remote) {
                return remote.getToken();
              }
            },
    };
    return new ListTableModel<>(columns, remotes);
  }

  private void configureCachingSettings() {
    cachingEnabledCheckBox = new JBCheckBox(CIAidBundle.message("settings.remotes.caching.enable-caching"));
    cachingEnabledCheckBox.setSelected(CIAidSettingsState.getInstance(project).isCachingEnabled());
    cachingEnabledCheckBox.addActionListener(e -> {
      boolean isSelected = cachingEnabledCheckBox.isSelected();
      cacheExpiryTimeTextField.setEnabled(isSelected);
      clearCacheButton.setEnabled(isSelected);
      cacheExpiryTimeLabel.setEnabled(isSelected);
    });
    cacheExpiryTimeTextField = new JBTextField();
    cacheExpiryTimeTextField.setText(String.valueOf(CIAidSettingsState.getInstance(project).getCacheExpiryTime()));
    cacheExpiryTimeTextField.setEnabled(cachingEnabledCheckBox.isSelected());
    cacheExpiryTimeTextField.getEmptyText().setText(CIAidBundle.message("settings.remotes.caching.expiry-time.empty-text"));
    cacheExpiryTimeTextField.setToolTipText(CIAidBundle.message("settings.remotes.caching.expiry-time.tooltip-text"));

    clearCacheButton = new JButton(CIAidBundle.message("settings.remotes.caching.clear-cache"));
    clearCacheButton.setEnabled(cachingEnabledCheckBox.isSelected());
    clearCacheButton.addActionListener(e -> CIAidCacheService.getInstance().clearCache());
    cachingSettingsPanel = new JPanel(new BorderLayout(5, 5));
    cachingSettingsPanel.add(cachingEnabledCheckBox, BorderLayout.NORTH);
    cacheExpiryTimeLabel = new JLabel(CIAidBundle.message("settings.remotes.caching.expiry-time") + ":");
    cacheExpiryTimeLabel.setEnabled(cachingEnabledCheckBox.isSelected());
    cachingSettingsPanel.add(cacheExpiryTimeLabel, BorderLayout.WEST);
    cachingSettingsPanel.add(cacheExpiryTimeTextField, BorderLayout.CENTER);
    cachingSettingsPanel.add(clearCacheButton, BorderLayout.EAST);
  }

  private static class RemoteDialog extends DialogWrapper {
    private final Project project;
    private Remote remote;

    private JBTextField gitlabApiUrlTextField;
    private JPanel gitlabApiUrlPanel;
    private JBTextField gitlabProjectTextField;
    private JPanel gitlabProjectPanel;
    private JBPasswordField gitlabAccessTokenField;
    private JPanel gitlabAccessTokenPanel;

    public RemoteDialog(@Nullable Project project, boolean canBeParent, Remote remote) {
      super(project, canBeParent);
      this.project = project;
      init();
      var isAdd = remote == null;
      var addOrEditText = isAdd ? CIAidBundle.message("settings.remotes.dialog.title.add") : CIAidBundle.message("settings.remotes.dialog.title.edit");
      setTitle(CIAidBundle.message("settings.remotes.dialog.title", addOrEditText));
      setSize(700, 200);
      setModal(true);
    }

    public Remote getRemote() {
      return remote;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
      configureDialogComponents();

      return FormBuilder.createFormBuilder()
              .addLabeledComponent(new JLabel(CIAidBundle.message("settings.remotes.table.gitlab-api-url-column") + ":"), gitlabApiUrlPanel)
              .addLabeledComponent(new JLabel(CIAidBundle.message("settings.remotes.table.project-column") + ":"), gitlabProjectPanel)
              .addLabeledComponent(new JLabel(CIAidBundle.message("settings.remotes.table.access-token-column") + ":"), gitlabAccessTokenPanel)
              .addComponentFillVertically(new JPanel(), 0)
              .getPanel();
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
      var apiUrl = gitlabApiUrlTextField.getText().trim();
      if (!CIAidUtils.isValidUrl(apiUrl)) {
        return new ValidationInfo(CIAidBundle.message("settings.remotes.dialog.gitlab-api-url.validation.error"), gitlabApiUrlTextField);
      }
      var projectPath = gitlabProjectTextField.getText().trim();
      var accessToken = new String(gitlabAccessTokenField.getPassword()).trim();
      if (projectPath.isEmpty()) {
        return new ValidationInfo(CIAidBundle.message("settings.remotes.dialog.project.validation.error"), gitlabProjectTextField);
      }
      if (accessToken.isEmpty()) {
        return new ValidationInfo(CIAidBundle.message("settings.remotes.dialog.access-token.validation.error"), gitlabAccessTokenField);
      }
      return null;
    }

    @Override
    protected void doOKAction() {
      if (remote == null) {
        remote = new Remote();
      }
      var projectPath = gitlabProjectTextField.getText().trim();
      remote.apiUrl(gitlabApiUrlTextField.getText().trim())
              .token(new String(gitlabAccessTokenField.getPassword()))
              .project(projectPath);
      CIAidSettingsState.getInstance(project).saveGitLabAccessToken(projectPath, new String(gitlabAccessTokenField.getPassword()));
      super.doOKAction();
    }

    @Override
    protected void dispose() {
      gitlabApiUrlTextField = null;
      gitlabApiUrlPanel = null;
      gitlabProjectTextField = null;
      gitlabProjectPanel = null;
      gitlabAccessTokenField = null;
      gitlabAccessTokenPanel = null;
      super.dispose();
    }

    private void configureDialogComponents() {
      //noinspection DialogTitleCapitalization
      var gitLabApiUrlTextFieldAndPanel = getTextFieldWithHelp(CIAidBundle.message("settings.remotes.dialog.gitlab-api-url.empty-text"),
              CIAidBundle.message("settings.remotes.dialog.gitlab-api-url.comment-text"));
      gitlabApiUrlTextField = gitLabApiUrlTextFieldAndPanel.first;
      gitlabApiUrlPanel = gitLabApiUrlTextFieldAndPanel.second;

      var gitLabProjectTextFieldAndPanel = getTextFieldWithHelp(CIAidBundle.message("settings.remotes.dialog.project.empty-text"),
              CIAidBundle.message("settings.remotes.dialog.project.comment-text"));
      gitlabProjectTextField = gitLabProjectTextFieldAndPanel.first;
      gitlabProjectPanel = gitLabProjectTextFieldAndPanel.second;

      gitlabAccessTokenField = new JBPasswordField();
      // noinspection DialogTitleCapitalization
      gitlabAccessTokenField.getEmptyText().setText(CIAidBundle.message("settings.remotes.dialog.access-token.empty-text"));
      var helpLabel = ContextHelpLabel.create(CIAidBundle.message("settings.remotes.dialog.access-token.comment-text"));

      gitlabAccessTokenPanel = new JPanel(new BorderLayout(5, 0));
      gitlabAccessTokenPanel.add(gitlabAccessTokenField, BorderLayout.CENTER);
      gitlabAccessTokenPanel.add(helpLabel, BorderLayout.EAST);
    }
  }

  private static Pair<JBTextField, JPanel> getTextFieldWithHelp(@NlsContexts.Label String labelText, @NlsContexts.Tooltip String helpText) {
    var textField = new JBTextField();
    textField.getEmptyText().setText(labelText);
    ContextHelpLabel helpLabel = ContextHelpLabel.create(helpText);
    JPanel panel = new JPanel(new BorderLayout(5, 0));
    panel.add(textField, BorderLayout.CENTER);
    panel.add(helpLabel, BorderLayout.EAST);
    return Pair.create(textField, panel);
  }
}
