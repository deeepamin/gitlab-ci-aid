package com.github.deeepamin.ciaid.settings.remotes;

import com.intellij.util.xmlb.annotations.Transient;

import java.io.Serializable;

public class Remote implements Serializable {
  // public attributes and getters/setters serialize the object to XML
  public String apiUrl;
  @Transient
  public String token;
  public String projectPath;

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

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

  @Transient
  public void setToken(String token) {
    this.token = token;
  }

  public Remote projectPath(String projectPath) {
    this.projectPath = projectPath;
    return this;
  }

  public String getProjectPath() {
    return projectPath;
  }
}
