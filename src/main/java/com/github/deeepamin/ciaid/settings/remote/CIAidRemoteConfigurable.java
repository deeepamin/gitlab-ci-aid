package com.github.deeepamin.ciaid.settings.remote;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.IntegerField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;

public class CIAidRemoteConfigurable implements Configurable {
  private static final int HOURS_IN_MONTH = 720;
  private final Project project;

  private JBTextField gitlabServerUrlField;
  private JPanel gitlabServerUrlPanel;
  private JBPasswordField gitlabAccessTokenField;
  private JPanel gitlabAccessTokenPanel;
  private JBTextField gitlabTemplatesProjectField;
  private JPanel gitlabTemplatesProjectPanel;
  private JBTextField gitlabTemplatesPathField;
  private JPanel gitlabTemplatesPathPanel;
  private JPanel cachingSettingsPanel;
  private JBCheckBox cachingEnabledCheckBox;
  private JLabel cacheExpiryTimeLabel;
  private IntegerField cacheExpiryTimeTextField;
  private JButton clearCacheButton;

  public CIAidRemoteConfigurable(Project project) {
    this.project = project;
  }

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return CIAidBundle.message("settings.remote.display.name");
  }

  @Override
  public @Nullable JComponent createComponent() {
    configureGitLabSettings();
    configureCachingSettings();

    return FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.remote.gitlab.config.separator")))
            .setFormLeftIndent(20)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.server-url") + ":"), gitlabServerUrlPanel)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.access-token") + ":"), gitlabAccessTokenPanel)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.templates.project") + ":"), gitlabTemplatesProjectPanel)
            .addLabeledComponent(new JLabel(CIAidBundle.message("settings.gitlab.templates.path") + ":"), gitlabTemplatesPathPanel)
            .setFormLeftIndent(0)
            .addComponent(new TitledSeparator(CIAidBundle.message("settings.remote.caching.separator")))
            .setFormLeftIndent(20)
            .addComponent(cachingSettingsPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }
  private void configureGitLabSettings() {
    //noinspection DialogTitleCapitalization
    var gitlabServerUrlFieldAndPanel = getTextFieldWithHelp(CIAidBundle.message("settings.gitlab.server-url.empty-text"),
            CIAidBundle.message("settings.gitlab.server-url.comment-text"));
    gitlabServerUrlField = gitlabServerUrlFieldAndPanel.first;
    gitlabServerUrlPanel = gitlabServerUrlFieldAndPanel.second;
    var serverUrlValidator = new ComponentValidator(() -> {})
            .withValidator(() -> {
              var serverUrl = gitlabServerUrlField.getText().trim();
              if (!CIAidUtils.isValidUrl(serverUrl)) {
                return new ValidationInfo(CIAidBundle.message("settings.gitlab.server-url.validation.error"), gitlabServerUrlField);
              }
              return null;
            })
            .installOn(gitlabServerUrlField);

    gitlabServerUrlField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        serverUrlValidator.revalidate();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        serverUrlValidator.revalidate();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        serverUrlValidator.revalidate();
      }
    });

    gitlabAccessTokenField = new JBPasswordField();
    //noinspection DialogTitleCapitalization
    gitlabAccessTokenField.getEmptyText().setText(CIAidBundle.message("settings.gitlab.access-token.empty-text"));
    ContextHelpLabel accessTokenHelpLabel = ContextHelpLabel.create(CIAidBundle.message("settings.gitlab.access-token.comment-text"));
    gitlabAccessTokenPanel = new JPanel(new BorderLayout(5, 0));
    gitlabAccessTokenPanel.add(gitlabAccessTokenField, BorderLayout.CENTER);
    gitlabAccessTokenPanel.add(accessTokenHelpLabel, BorderLayout.EAST);

    var accessTokenValidator = new ComponentValidator(() -> {})
            .withValidator(() -> {
              var accessToken = new String(gitlabAccessTokenField.getPassword());
              if (accessToken.isEmpty()) {
                return new ValidationInfo(CIAidBundle.message("settings.gitlab.access-token.validation.error"), gitlabAccessTokenField);
              }
              return null;
            })
            .installOn(gitlabServerUrlField);

    gitlabAccessTokenField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        accessTokenValidator.revalidate();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        accessTokenValidator.revalidate();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        accessTokenValidator.revalidate();
      }
    });

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
    long cacheExpiryTime;
    try {
      cacheExpiryTime = Long.parseLong(cacheExpiryTimeTextField.getText().trim());
    } catch (NumberFormatException e) {
      return false;
    }
    return !ciaidSettingsState.getGitLabServerUrl().equals(gitlabServerUrlField.getText().trim())
            || !ciaidSettingsState.getGitLabAccessToken().equals(new String(gitlabAccessTokenField.getPassword()))
            || ciaidSettingsState.getGitlabTemplatesProject() != null
                  && !ciaidSettingsState.getGitlabTemplatesProject().equals(gitlabTemplatesProjectField.getText().trim())
            || ciaidSettingsState.getGitlabTemplatesPath() != null
                  && !ciaidSettingsState.getGitlabTemplatesPath().equals(gitlabTemplatesPathField.getText().trim())
            || ciaidSettingsState.isCachingEnabled() != cachingEnabledCheckBox.isSelected()
            || !ciaidSettingsState.getCacheExpiryTime().equals(cacheExpiryTime);
  }

  @Override
  public void apply() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    var serverUrl = gitlabServerUrlField.getText().trim();
    ciaidSettingsState.setGitLabServerUrl(serverUrl);
    var accessToken = new String(gitlabAccessTokenField.getPassword());
    ciaidSettingsState.setGitLabAccessToken(accessToken);
    ciaidSettingsState.saveGitLabAccessToken(serverUrl, accessToken);
    ciaidSettingsState.setGitlabTemplatesProject(gitlabTemplatesProjectField.getText().trim());
    ciaidSettingsState.setGitlabTemplatesPath(gitlabTemplatesPathField.getText().trim());
    ciaidSettingsState.setCachingEnabled(cachingEnabledCheckBox.isSelected());
    try {
      long cacheExpiryTime = Long.parseLong(cacheExpiryTimeTextField.getText().trim());
      ciaidSettingsState.setCacheExpiryTime(cacheExpiryTime);
    } catch (NumberFormatException ignored) {
    }
    ciaidSettingsState.forceReadPluginData();
  }

  @Override
  public void reset() {
    var ciaidSettingsState = CIAidSettingsState.getInstance(project);
    gitlabServerUrlField.setText(ciaidSettingsState.getGitLabServerUrl());
    gitlabAccessTokenField.setText(ciaidSettingsState.getGitLabAccessToken());
    gitlabTemplatesProjectField.setText(ciaidSettingsState.getGitlabTemplatesProject());
    gitlabTemplatesPathField.setText(ciaidSettingsState.getGitlabTemplatesPath());

    cachingEnabledCheckBox.setSelected(ciaidSettingsState.isCachingEnabled());
    cacheExpiryTimeTextField.setText(String.valueOf(ciaidSettingsState.getCacheExpiryTime()));
  }

  private void configureCachingSettings() {
    cachingEnabledCheckBox = new JBCheckBox(CIAidBundle.message("settings.remote.caching.enable-caching"));
    cachingEnabledCheckBox.setSelected(CIAidSettingsState.getInstance(project).isCachingEnabled());
    cachingEnabledCheckBox.addActionListener(e -> {
      boolean isSelected = cachingEnabledCheckBox.isSelected();
      cacheExpiryTimeTextField.setEnabled(isSelected);
      clearCacheButton.setEnabled(isSelected);
      cacheExpiryTimeLabel.setEnabled(isSelected);
    });

    cacheExpiryTimeTextField = new IntegerField(String.valueOf(CIAidSettingsState.getInstance(project).getCacheExpiryTime()), 0, HOURS_IN_MONTH);
    cacheExpiryTimeTextField.setEnabled(cachingEnabledCheckBox.isSelected());
    cacheExpiryTimeTextField.getEmptyText().setText(CIAidBundle.message("settings.remote.caching.expiry-time.empty-text"));
    cacheExpiryTimeTextField.setToolTipText(CIAidBundle.message("settings.remote.caching.expiry-time.tooltip-text"));
    var expiryTimeNumberValidator = new ComponentValidator(() -> {})
            .withValidator(() -> {
              String text = cacheExpiryTimeTextField.getText();
              try {
                var value = Long.parseLong(text);
                if (value <= 0 || value > HOURS_IN_MONTH)  {
                  return new ValidationInfo(CIAidBundle.message("settings.remote.caching.expiry-time.validation.error"), cacheExpiryTimeTextField);
                }
              } catch (NumberFormatException e) {
                return new ValidationInfo(CIAidBundle.message("settings.remote.caching.expiry-time.validation.error"), cacheExpiryTimeTextField);
              }

              return null;
            })
            .installOn(cacheExpiryTimeTextField);

    cacheExpiryTimeTextField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        expiryTimeNumberValidator.revalidate();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        expiryTimeNumberValidator.revalidate();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        expiryTimeNumberValidator.revalidate();
      }
    });

    clearCacheButton = new JButton(CIAidBundle.message("settings.remote.caching.clear-cache"));
    clearCacheButton.setEnabled(cachingEnabledCheckBox.isSelected());
    clearCacheButton.addActionListener(e -> CIAidCacheService.getInstance().clearCache());
    cachingSettingsPanel = new JPanel(new BorderLayout(5, 5));
    cachingSettingsPanel.add(cachingEnabledCheckBox, BorderLayout.NORTH);
    cacheExpiryTimeLabel = new JLabel(CIAidBundle.message("settings.remote.caching.expiry-time") + ":");
    cacheExpiryTimeLabel.setEnabled(cachingEnabledCheckBox.isSelected());
    cachingSettingsPanel.add(cacheExpiryTimeLabel, BorderLayout.WEST);
    cachingSettingsPanel.add(cacheExpiryTimeTextField, BorderLayout.CENTER);
    cachingSettingsPanel.add(clearCacheButton, BorderLayout.EAST);
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
