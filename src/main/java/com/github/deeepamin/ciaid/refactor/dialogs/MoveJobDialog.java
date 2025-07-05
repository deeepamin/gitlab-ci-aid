package com.github.deeepamin.ciaid.refactor.dialogs;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.panel.ComponentPanelBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.FormBuilder;

import javax.swing.JLabel;
import java.util.List;

public class MoveJobDialog extends BaseRefactorToFileDialog {
  private JLabel moveJobCommentLabel;

  public MoveJobDialog(Project project, String jobName, List<VirtualFile> filteredFiles) {
    super(project, jobName, filteredFiles);
  }

  @Override
  protected String getRefactoringType() {
    return CIAidBundle.message("refactoring.move.job.dialog.title");
  }

  @SuppressWarnings("deprecation")
  public void configureDialogContents() {
    super.configureDialogContents();
    var commentText = CIAidBundle.message("refactoring.move.job.dialog.comment.text");
    moveJobCommentLabel = ComponentPanelBuilder.createCommentComponent(commentText, true);
  }

  @Override
  protected void customizeFormBuilder(FormBuilder formBuilder) {
    super.customizeFormBuilder(formBuilder);
    formBuilder.setFormLeftIndent(75)
            .addComponent(moveJobCommentLabel)
            .setFormLeftIndent(0);
  }

}
