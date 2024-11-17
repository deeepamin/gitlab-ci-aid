package com.github.deeepamin.gitlabciaid.services;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
public final class DisposerService implements Disposable {
  @Override
  public void dispose() {
  }

  public static DisposerService getInstance(final Project project) {
    return project.getService(DisposerService.class);
  }
}
