package com.github.deeepamin.ciaid.model;

import com.github.deeepamin.ciaid.model.gitlab.include.IncludeFile;
import com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.ArrayList;
import java.util.List;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

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

  public List<PsiElement> getJobStageElements() {
    return jobStageElements.stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(SmartPsiElementPointer::getElement)
            .toList();
  }

  public void addJobStage(YAMLPsiElement stage) {
    var stageName = stage.getText();
    if (InputsReferenceProvider.isAnInputsString(stageName)) {
      return;
    }
    SmartPointerManager pointerManager = SmartPointerManager.getInstance(stage.getProject());
    SmartPsiElementPointer<PsiElement> stagePointer = pointerManager.createSmartPsiElementPointer(stage);
    jobStageElements.add(stagePointer);
  }

  public List<YAMLKeyValue> getJobElements() {
    return jobElements.stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(SmartPsiElementPointer::getElement)
            .toList();
  }

  public void addJob(YAMLKeyValue job) {
    var jobName = job.getKeyText();
    if (InputsReferenceProvider.isAnInputsString(jobName)) {
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

  public List<YAMLKeyValue> getInputs() {
    return inputs.stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(SmartPsiElementPointer::getElement)
            .toList();
  }

  public List<YAMLPsiElement> getStagesItemElements() {
    return stagesItemElements.stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(SmartPsiElementPointer::getElement)
            .toList();
  }

  public void addStagesItem(YAMLPsiElement stagesItemElement) {
    var stagesItemName = handleQuotedText(stagesItemElement.getText());
    if (InputsReferenceProvider.isAnInputsString(stagesItemName)) {
      return;
    }
    SmartPointerManager pointerManager = SmartPointerManager.getInstance(stagesItemElement.getProject());
    SmartPsiElementPointer<YAMLPsiElement> stagesItemPointer = pointerManager.createSmartPsiElementPointer(stagesItemElement);
    stagesItemElements.add(stagesItemPointer);
  }

  public List<YAMLKeyValue> getVariables() {
    return variables.stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(SmartPsiElementPointer::getElement)
            .toList();
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

