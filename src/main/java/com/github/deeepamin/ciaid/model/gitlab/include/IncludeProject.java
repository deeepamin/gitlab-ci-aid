package com.github.deeepamin.ciaid.model.gitlab.include;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public final class IncludeProject extends IncludeFile {
  private String project;
  private String ref;

  public void setProject(String project) {
    this.project = handleQuotedText(project);
  }

  public void setRef(String ref) {
    this.ref = handleQuotedText(ref);
  }

  public String getProject() {
    return project;
  }

  public String getRef() {
    return ref;
  }
}
