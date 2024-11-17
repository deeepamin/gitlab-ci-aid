package com.github.deeepamin.gitlabciaid.language;

import com.github.deeepamin.gitlabciaid.model.Icons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;

public class GitlabCIYamlFileType extends LanguageFileType {
  protected GitlabCIYamlFileType() {
    super(YAMLLanguage.INSTANCE, true);
  }

  @Override
  public @NonNls @NotNull String getName() {
    return "Gitlab CI YAML";
  }

  @Override
  public @NlsContexts.Label @NotNull String getDescription() {
    return "Gitlab CI YAML";
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "gitlab-ci.yml";
  }

  @Override
  public Icon getIcon() {
    return Icons.ICON_GITLAB_LOGO.getIcon();
  }
}
