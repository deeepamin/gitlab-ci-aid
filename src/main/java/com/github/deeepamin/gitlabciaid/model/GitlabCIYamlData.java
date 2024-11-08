package com.github.deeepamin.gitlabciaid.model;

import com.github.deeepamin.gitlabciaid.model.yaml.Job;
import com.github.deeepamin.gitlabciaid.model.yaml.Stage;

import java.util.HashMap;
import java.util.Map;

public class GitlabCIYamlData {
  private final String path;
  private final Map<String, Stage> stages;
  private final Map<String, Job> jobs;

  public GitlabCIYamlData(String path) {
    this.path = path;
    this.stages = new HashMap<>();
    this.jobs = new HashMap<>();
  }

  public Map<String, Stage> getStages() {
    return stages;
  }

  public void addStage(Stage stage) {
    stages.put(stage.getName(), stage);
  }

  public Map<String, Job> getJobs() {
    return jobs;
  }

  public void addJob(Job job) {
    jobs.put(job.getName(), job);
  }
}

