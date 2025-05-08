package com.github.deeepamin.ciaid.model;

import com.github.deeepamin.ciaid.model.gitlab.Input;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.ReferenceUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitlabCIYamlData {
  //.gitlab-ci.yml path to data mapping, also for included files
  private final VirtualFile file;
  private final Map<String, List<SmartPsiElementPointer<PsiElement>>> stageNameToStageElements;
  private final Map<String, SmartPsiElementPointer<YAMLKeyValue>> jobNameToJobElement;
  private final List<String> includedYamls;
  private final long modificationStamp;
  private final Map<String, SmartPsiElementPointer<YAMLPsiElement>> stagesItemNameToStagesElement;
  private final List<Input> inputs;

  public GitlabCIYamlData(VirtualFile file, long modificationStamp) {
    this.file = file;
    this.modificationStamp = modificationStamp;
    this.stageNameToStageElements = new HashMap<>();
    this.jobNameToJobElement = new HashMap<>();
    this.includedYamls = new ArrayList<>();
    this.stagesItemNameToStagesElement = new HashMap<>();
    this.inputs = new ArrayList<>();
  }

  public VirtualFile getFile() {
    return file;
  }

  public Map<String, List<SmartPsiElementPointer<PsiElement>>> getStageNameToStageElements() {
    return stageNameToStageElements;
  }

  public void addStage(YAMLPsiElement stage) {
    if (stage instanceof YAMLKeyValue yamlKeyValueStage) {
      var stageName = yamlKeyValueStage.getValueText();
      if (GitlabCIYamlUtils.isAnInputsString(stageName)) {
        return;
      }
      var stageNameRefs = stageNameToStageElements.getOrDefault(stageName, new ArrayList<>());
      SmartPointerManager pointerManager = SmartPointerManager.getInstance(stage.getProject());
      SmartPsiElementPointer<PsiElement> stagePointer = pointerManager.createSmartPsiElementPointer(stage);
      stageNameRefs.add(stagePointer);
      stageNameToStageElements.put(stageName, stageNameRefs);
    }
  }

  public Map<String, SmartPsiElementPointer<YAMLKeyValue>> getJobNameToJobElement() {
    return jobNameToJobElement;
  }

  public void addJob(YAMLKeyValue job) {
    var jobName = job.getKeyText();
    if (GitlabCIYamlUtils.isAnInputsString(jobName)) {
      return;
    }
    SmartPointerManager pointerManager = SmartPointerManager.getInstance(job.getProject());
    SmartPsiElementPointer<YAMLKeyValue> jobPointer = pointerManager.createSmartPsiElementPointer(job);
    jobNameToJobElement.put(jobName, jobPointer);
  }

  public void addIncludedYaml(String yaml) {
    includedYamls.add(yaml);
  }

  public List<String> getIncludedYamls() {
    return includedYamls;
  }

  public void addInput(Input input) {
    if (input != null) {
      inputs.add(input);
    }
  }

  public List<Input> getInputs() {
    return inputs;
  }

  public Map<String, SmartPsiElementPointer<YAMLPsiElement>> getStagesItemNameToStagesElement() {
    return stagesItemNameToStagesElement;
  }

  public void addStagesItem(YAMLPsiElement stagesItemElement) {
    var stagesItemName = ReferenceUtils.handleQuotedText(stagesItemElement.getText());
    if (GitlabCIYamlUtils.isAnInputsString(stagesItemName) || stagesItemNameToStagesElement.containsKey(stagesItemName)) {
      return;
    }
    SmartPointerManager pointerManager = SmartPointerManager.getInstance(stagesItemElement.getProject());
    SmartPsiElementPointer<YAMLPsiElement> stagesItemPointer = pointerManager.createSmartPsiElementPointer(stagesItemElement);
    stagesItemNameToStagesElement.put(stagesItemElement.getText(), stagesItemPointer);
  }

  private long getModificationStamp() {
    return modificationStamp;
  }

  public boolean isUpToDate(VirtualFile newFile) {
    return newFile.getModificationStamp() == this.getModificationStamp();
  }
}

