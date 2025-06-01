package com.github.deeepamin.ciaid.settings.remotes;

import com.intellij.util.xmlb.annotations.Transient;

import java.io.Serializable;

public class Remote implements Serializable {
  private String apiUrl;
  @Transient
  private String token;
  private String projectPath;

  public Remote apiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
    return this;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public Remote token(String token) {
    this.token = token;
    return this;
  }

  @Transient
  public String getToken() {
    return token;
  }

  public Remote projectPath(String projectPath) {
    this.projectPath = projectPath;
    return this;
  }

  public String getProjectPath() {
    return projectPath;
  }
}
