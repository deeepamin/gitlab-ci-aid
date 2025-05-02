package com.github.deeepamin.ciaid.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public enum Icons {
  ICON_NEEDS(AllIcons.Nodes.Related),
  ICON_STAGE(AllIcons.Debugger.VariablesTab),
  ICON_GITLAB_LOGO(IconLoader.getIcon("/icons/gitlabLogo.svg", Icons.class)),;

  private final Icon icon;

  Icons(final Icon icon) {
    this.icon = icon;
  }

  public Icon getIcon() {
    return icon;
  }
}
