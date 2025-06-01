package com.github.deeepamin.ciaid.cache.providers;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public abstract class AbstractIncludeProvider {
  protected final Logger LOG = Logger.getInstance(this.getClass());
  protected final Project project;
  protected final String filePath;

  protected AbstractIncludeProvider(Project project, String filePath) {
    this.project = project;
    this.filePath = filePath;
  }

  public abstract void readIncludeFile();
}
