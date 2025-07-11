package com.github.deeepamin.ciaid.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;

public class CIAidUserMarkedFilesTablePathEditor extends AbstractCellEditor implements TableCellEditor {
  TextFieldWithBrowseButton editorComponent;

  public CIAidUserMarkedFilesTablePathEditor(Project project)   {
    editorComponent = new TextFieldWithBrowseButton();
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.singleFile();
    editorComponent.addBrowseFolderListener(project, descriptor);
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    editorComponent.setText(value != null ? value.toString() : "");
    return editorComponent;
  }

  @Override
  public Object getCellEditorValue() {
    return editorComponent.getText();
  }
}
