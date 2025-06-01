package com.github.deeepamin.ciaid.settings.remotes;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

public class AccessTokenCellRenderer extends DefaultTableCellRenderer {
  public static final String HIDDEN_TEXT = "**********";
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (value != null && !value.toString().isEmpty()) {
      setText(HIDDEN_TEXT);
    } else {
      setText("");
    }

    return this;
  }
}
