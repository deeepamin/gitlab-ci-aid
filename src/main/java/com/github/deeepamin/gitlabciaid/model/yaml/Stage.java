package com.github.deeepamin.gitlabciaid.model.yaml;

import java.util.HashMap;
import java.util.Map;

public class Stage {
  private String name;
  private Map<String, Job> jobs;

  public Stage() {
    this.jobs = new HashMap<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Job> getJobs() {
    return jobs;
  }

  public void setJobs(Map<String, Job> jobs) {
    this.jobs = jobs;
  }
}
