package com.github.deeepamin.gitlabciaid.model;

import com.intellij.icons.AllIcons;
import javax.swing.Icon;

public enum Icons {
  ICON_NEEDS(AllIcons.Nodes.Related),
  ICON_STAGE(AllIcons.Debugger.VariablesTab);
  private final Icon icon;

  Icons(final Icon icon) {
    this.icon = icon;
  }

  public Icon getIcon() {
    return icon;
  }
}
