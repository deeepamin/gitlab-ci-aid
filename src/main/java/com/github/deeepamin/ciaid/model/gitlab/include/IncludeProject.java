package com.github.deeepamin.ciaid.model.gitlab.include;

public final class IncludeProject extends IncludeFile {
  private String project;
  private String ref;

  public void setProject(String project) {
    this.project = project;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public String getProject() {
    return project;
  }

  public String getRef() {
    return ref;
  }
}
