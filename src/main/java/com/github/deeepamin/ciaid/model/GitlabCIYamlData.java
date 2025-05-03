package com.github.deeepamin.ciaid.model;

import com.github.deeepamin.ciaid.utils.ReferenceUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitlabCIYamlData {
  //.gitlab-ci.yml path to data mapping, also for included files
  private final VirtualFile file;
  private final Map<String, List<PsiElement>> stageNameToStageElements;
  private final Map<String, YAMLKeyValue> jobNameToJobElement;
  private final List<String> includedYamls;
  private final long modificationStamp;
  private final Map<String, YAMLPsiElement> stagesItemNameToStagesElement;

  public GitlabCIYamlData(VirtualFile file, long modificationStamp) {
    this.file = file;
    this.modificationStamp = modificationStamp;
    this.stageNameToStageElements = new HashMap<>();
    this.jobNameToJobElement = new HashMap<>();
    this.includedYamls = new ArrayList<>();
    this.stagesItemNameToStagesElement = new HashMap<>();
  }

  public VirtualFile getFile() {
    return file;
  }

  public Map<String, List<PsiElement>> getStageNameToStageElements() {
    return stageNameToStageElements;
  }

  public void addStage(YAMLPsiElement stage) {
    if (stage instanceof YAMLKeyValue yamlKeyValueStage) {
      var stageName = yamlKeyValueStage.getValueText();
      var stageNameRefs = stageNameToStageElements.getOrDefault(stageName, new ArrayList<>());
      stageNameRefs.add(stage);
      stageNameToStageElements.put(stageName, stageNameRefs);
    }
  }

  public Map<String, YAMLKeyValue> getJobNameToJobElement() {
    return jobNameToJobElement;
  }

  public void addJob(YAMLKeyValue job) {
    jobNameToJobElement.put(job.getKeyText(), job);
  }

  public void addIncludedYaml(String yaml) {
    includedYamls.add(yaml);
  }

  public List<String> getIncludedYamls() {
    return includedYamls;
  }

  public Map<String, YAMLPsiElement> getStagesItemNameToStagesElement() {
    return stagesItemNameToStagesElement;
  }

  public void addStagesItem(YAMLPsiElement stagesItemElement) {
    var stagesItemName = ReferenceUtils.handleQuotedText(stagesItemElement.getText());
    if (stagesItemNameToStagesElement.containsKey(stagesItemName)) {
      return;
    }
    stagesItemNameToStagesElement.put(stagesItemElement.getText(), stagesItemElement);
  }

  private long getModificationStamp() {
    return modificationStamp;
  }

  public boolean isUpToDate(VirtualFile newFile) {
    return newFile.getModificationStamp() == this.getModificationStamp();
  }
}

