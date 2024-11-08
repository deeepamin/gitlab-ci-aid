package com.github.deeepamin.gitlabciaid.model;

import com.intellij.psi.PsiElement;

import java.util.HashMap;
import java.util.Map;

public class GitlabCIYamlData {
  private final String path;
  private final Map<String, PsiElement> stages;
  private final Map<String, PsiElement> jobs;

  public GitlabCIYamlData(String path) {
    this.path = path;
    this.stages = new HashMap<>();
    this.jobs = new HashMap<>();
  }

  public Map<String, PsiElement> getStages() {
    return stages;
  }

  public void addStage(PsiElement stage) {
    stages.put(stage.getText(), stage);
  }

  public Map<String, PsiElement> getJobs() {
    return jobs;
  }

  public void addJob(PsiElement job) {
    jobs.put(job.getText(), job);
  }
}

