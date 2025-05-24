package com.github.deeepamin.ciaid.model;

import com.github.deeepamin.ciaid.model.gitlab.include.IncludeFile;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.ArrayList;
import java.util.List;

import static com.github.deeepamin.ciaid.utils.ReferenceUtils.handleQuotedText;

public class CIAidYamlData {
  //.gitlab-ci.yml path to data mapping, also for included files
  private final VirtualFile file;
  private final long modificationStamp;
  private final List<IncludeFile> includes;
  private final List<SmartPsiElementPointer<PsiElement>> jobStageElements;
  private final List<SmartPsiElementPointer<YAMLKeyValue>> jobElements;
  private final List<SmartPsiElementPointer<YAMLPsiElement>> stagesItemElements;
  private final List<SmartPsiElementPointer<YAMLKeyValue>> inputs;
  private final List<SmartPsiElementPointer<YAMLKeyValue>> variables;

  public CIAidYamlData(VirtualFile file, long modificationStamp) {
    this.file = file;
    this.modificationStamp = modificationStamp;
    this.includes = new ArrayList<>();
    this.jobStageElements = new ArrayList<>();
    this.jobElements = new ArrayList<>();
    this.stagesItemElements = new ArrayList<>();
    this.inputs = new ArrayList<>();
    this.variables = new ArrayList<>();
  }

  public VirtualFile getFile() {
    return file;
  }

  public void addInclude(IncludeFile includeFile) {
    if (includeFile != null) {
      includes.add(includeFile);
    }
  }

  public List<IncludeFile> getIncludes() {
    return includes;
  }

  public List<SmartPsiElementPointer<PsiElement>> getJobStageElements() {
    return jobStageElements;
  }

  public void addJobStage(YAMLPsiElement stage) {
    var stageName = stage.getText();
    if (GitlabCIYamlUtils.isAnInputsString(stageName)) {
      return;
    }
    SmartPointerManager pointerManager = SmartPointerManager.getInstance(stage.getProject());
    SmartPsiElementPointer<PsiElement> stagePointer = pointerManager.createSmartPsiElementPointer(stage);
    jobStageElements.add(stagePointer);
  }

  public List<SmartPsiElementPointer<YAMLKeyValue>> getJobElements() {
    return jobElements;
  }

  public void addJob(YAMLKeyValue job) {
    var jobName = job.getKeyText();
    if (GitlabCIYamlUtils.isAnInputsString(jobName)) {
      return;
    }
    SmartPointerManager pointerManager = SmartPointerManager.getInstance(job.getProject());
    SmartPsiElementPointer<YAMLKeyValue> jobPointer = pointerManager.createSmartPsiElementPointer(job);
    jobElements.add(jobPointer);
  }

  public void addInput(YAMLKeyValue input) {
    if (input != null) {
      SmartPointerManager pointerManager = SmartPointerManager.getInstance(input.getProject());
      SmartPsiElementPointer<YAMLKeyValue> inputPointer = pointerManager.createSmartPsiElementPointer(input);
      inputs.add(inputPointer);
    }
  }

  public List<SmartPsiElementPointer<YAMLKeyValue>> getInputs() {
    return inputs;
  }

  public List<SmartPsiElementPointer<YAMLPsiElement>> getStagesItemElements() {
    return stagesItemElements;
  }

  public void addStagesItem(YAMLPsiElement stagesItemElement) {
    var stagesItemName = handleQuotedText(stagesItemElement.getText());
    if (GitlabCIYamlUtils.isAnInputsString(stagesItemName)) {
      return;
    }
    SmartPointerManager pointerManager = SmartPointerManager.getInstance(stagesItemElement.getProject());
    SmartPsiElementPointer<YAMLPsiElement> stagesItemPointer = pointerManager.createSmartPsiElementPointer(stagesItemElement);
    stagesItemElements.add(stagesItemPointer);
  }

  public List<SmartPsiElementPointer<YAMLKeyValue>> getVariables() {
    return variables;
  }

  public void addVariable(YAMLKeyValue variable) {
    if (variable != null) {
      SmartPointerManager pointerManager = SmartPointerManager.getInstance(variable.getProject());
      SmartPsiElementPointer<YAMLKeyValue> variablePointer = pointerManager.createSmartPsiElementPointer(variable);
      variables.add(variablePointer);
    }
  }

  private long getModificationStamp() {
    return modificationStamp;
  }

  public boolean isUpToDate(VirtualFile newFile) {
    return newFile.getModificationStamp() == this.getModificationStamp();
  }
}

