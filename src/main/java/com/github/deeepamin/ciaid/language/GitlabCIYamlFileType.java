package com.github.deeepamin.ciaid.language;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.model.Icons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.Icon;

public class GitlabCIYamlFileType extends LanguageFileType {
  private static final String DEFAULT_FILE_NAME = "gitlab-ci.yml";
  protected GitlabCIYamlFileType() {
    super(YAMLLanguage.INSTANCE, true);
  }

  @Override
  public @NonNls @NotNull String getName() {
    return CIAidBundle.message("file.type.ci.yaml.name");
  }

  @Override
  public @NlsContexts.Label @NotNull String getDescription() {
    return CIAidBundle.message("file.type.ci.yaml.description");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return DEFAULT_FILE_NAME;
  }

  @Override
  public Icon getIcon() {
    return Icons.ICON_GITLAB_LOGO.getIcon();
  }
}
