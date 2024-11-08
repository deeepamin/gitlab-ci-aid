package com.github.deeepamin.gitlabciaid.model;

import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.HashMap;
import java.util.Map;

public class GitlabCIYamlData {
  private final String path;
  private final Map<String, YAMLKeyValue> stages;
  private final Map<String, YAMLKeyValue> jobs;

  public GitlabCIYamlData(String path) {
    this.path = path;
    this.stages = new HashMap<>();
    this.jobs = new HashMap<>();
  }

  public Map<String, YAMLKeyValue> getStages() {
    return stages;
  }

  public void addStage(YAMLKeyValue stage) {
    stages.put(stage.getKeyText(), stage);
  }

  public Map<String, YAMLKeyValue> getJobs() {
    return jobs;
  }

  public void addJob(YAMLKeyValue job) {
    jobs.put(job.getKeyText(), job);
  }
}

