package com.github.deeepamin.ciaid.model;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.EmptyIcon;

import javax.swing.Icon;

public enum Icons {
  ICON_NEEDS,
  ICON_STAGE,
  ICON_YAML,
  ICON_BOOLEAN,
  ICON_NUMBER,
  ICON_STRING,
  ICON_VARIABLE,
  ICON_ARRAY,
  ICON_GITLAB_LOGO;

  private volatile Icon loadedIcon;

  public Icon getIcon() {
    var result = loadedIcon;
    if (result != null) {
      return result;
    }

    synchronized (this) {
      result = loadedIcon;
      if (result == null) {
        try {
          result = switch (this) {
            case ICON_NEEDS -> AllIcons.Nodes.Related;
            case ICON_STAGE -> AllIcons.Debugger.VariablesTab;
            case ICON_YAML -> AllIcons.FileTypes.Yaml;
            case ICON_BOOLEAN -> AllIcons.Actions.Checked;
            case ICON_NUMBER -> AllIcons.FileTypes.JavaClass;
            case ICON_STRING -> AllIcons.FileTypes.Font;
            case ICON_VARIABLE -> AllIcons.Nodes.Variable;
            case ICON_ARRAY -> IconLoader.getIcon("/icons/list.svg", Icons.class);
            case ICON_GITLAB_LOGO -> IconLoader.getIcon("/icons/gitlabLogo.svg", Icons.class);
          };
          loadedIcon = result;
        } catch (ReadAction.CannotReadException e) {
          return EmptyIcon.create(16);
        }
      }
    }
    return result;
  }
}
