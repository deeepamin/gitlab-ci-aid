package com.github.deeepamin.ciaid.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public enum Icons {
  ICON_NEEDS(AllIcons.Nodes.Related),
  ICON_STAGE(AllIcons.Debugger.VariablesTab),
  ICON_BOOLEAN(AllIcons.Actions.Checked),
  ICON_NUMBER(AllIcons.FileTypes.JavaClass),
  ICON_STRING(AllIcons.FileTypes.Font),
  ICON_ARRAY(IconLoader.getIcon("/icons/list.svg", Icons.class)),
  ICON_GITLAB_LOGO(IconLoader.getIcon("/icons/gitlabLogo.svg", Icons.class)),;

  private final Icon icon;

  Icons(final Icon icon) {
    this.icon = icon;
  }

  public Icon getIcon() {
    return icon;
  }
}
