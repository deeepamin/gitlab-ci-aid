package com.github.deeepamin.ciaid.refactor.actions;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

public abstract class BaseRefactorAction extends AnAction {
  @Override
  public void update(@NotNull AnActionEvent e) {
    PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
    e.getPresentation().setEnabledAndVisible(isAvailable(element));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var element = e.getData(CommonDataKeys.PSI_ELEMENT);
    if (!isAvailable(element)) {
      return;
    }
    var isGitLabCIYaml = CIAidProjectService.hasGitlabYamlFile(element);
    if (!isGitLabCIYaml) {
      return;
    }
    performRefactorAction(e);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  protected YAMLMapping createYamlMapping(Project project, YAMLKeyValue keyValue) {
    var keyValueText = keyValue.getText();
    var dummyFile = (YAMLFile) PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.yaml", YAMLFileType.YML, keyValueText);

    var dummyDoc = dummyFile.getDocuments().getFirst();
    if (dummyDoc == null || !(dummyDoc.getTopLevelValue() instanceof YAMLMapping yamlMapping)) {
      return null;
    }
    return yamlMapping;
  }

  protected YAMLKeyValue createYamlKeyValue(Project project, String key, String value) {
    String yamlText = key + ": " + value;
    var dummyFile = YAMLElementGenerator.getInstance(project).createDummyYamlWithText(yamlText);
    return PsiTreeUtil.findChildOfType(dummyFile, YAMLKeyValue.class);
  }

  protected abstract boolean isAvailable(PsiElement element);

  protected abstract void performRefactorAction(@NotNull AnActionEvent e);
}
