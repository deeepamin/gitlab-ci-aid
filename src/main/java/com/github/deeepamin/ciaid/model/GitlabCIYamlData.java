package com.github.deeepamin.ciaid.model;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitlabCIYamlData {
  //.gitlab-ci.yml path to data mapping, also for included files
  private final VirtualFile file;
  private final Map<String, List<PsiElement>> stages;
  private final Map<String, YAMLKeyValue> jobs;
  private final List<String> includedYamls;
  private final long modificationStamp;
  private YAMLKeyValue stagesElement;

  public GitlabCIYamlData(VirtualFile file, long modificationStamp) {
    this.file = file;
    this.modificationStamp = modificationStamp;
    this.stages = new HashMap<>();
    this.jobs = new HashMap<>();
    this.includedYamls = new ArrayList<>();
  }

  public VirtualFile getFile() {
    return file;
  }

  public Map<String, List<PsiElement>> getStages() {
    return stages;
  }

  public void addStage(PsiElement stage) {
    if (stage instanceof YAMLKeyValue yamlKeyValueStage) {
      var stageName = yamlKeyValueStage.getValueText();
      var stageNameRefs = stages.getOrDefault(stageName, new ArrayList<>());
      stageNameRefs.add(stage);
      stages.put(stageName, stageNameRefs);
    }
  }

  public Map<String, YAMLKeyValue> getJobs() {
    return jobs;
  }

  public void addJob(YAMLKeyValue job) {
    jobs.put(job.getKeyText(), job);
  }

  public void addIncludedYaml(String yaml) {
    includedYamls.add(yaml);
  }

  public List<String> getIncludedYamls() {
    return includedYamls;
  }

  public YAMLKeyValue getStagesElement() {
    return stagesElement;
  }

  public void setStagesElement(YAMLKeyValue stagesElement) {
    this.stagesElement = stagesElement;
  }

  private long getModificationStamp() {
    return modificationStamp;
  }

  public boolean isUpToDate(VirtualFile newFile) {
    return newFile.getModificationStamp() == this.getModificationStamp();
  }
}

