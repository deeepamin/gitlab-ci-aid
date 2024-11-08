package com.github.deeepamin.gitlabciaid.model.yaml;

import java.util.ArrayList;
import java.util.List;

public class Job {
  private String name;
  private String stageName;
  private List<String> beforeScript;
  private List<String> script;
  private List<String> afterScript;
  private List<String> needs;

  public Job() {
    beforeScript = new ArrayList<>();
    script = new ArrayList<>();
    afterScript = new ArrayList<>();
    needs = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStage() {
    return stageName;
  }

  public void setStageName(String stageName) {
    this.stageName = stageName;
  }

  public List<String> getBeforeScript() {
    return beforeScript;
  }

  public void addBeforeScript(String beforeScript) {
    this.beforeScript.add(beforeScript);
  }

  public List<String> getScript() {
    return script;
  }

  public void addAfterScript(String afterScript) {
    this.afterScript.add(afterScript);
  }

  public List<String> getAfterScript() {
    return afterScript;
  }

  public void addScript(String script) {
    this.script.add(script);
  }

  public List<String> getNeeds() {
    return needs;
  }

  public void addNeed(String need) {
    this.needs.add(need);
  }
}
