package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.intellij.ide.trustedProjects.TrustedProjects;
import com.intellij.ide.trustedProjects.TrustedProjectsListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class CIAidPostStartup implements ProjectActivity {
  @Override
  public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
    final var projectService = CIAidProjectService.getInstance(project);
    CIAidProjectService.executeOnThreadPool(project, () -> {
      CIAidCacheService.getInstance().loadCacheFromDisk(project);
    });
    initializeWhenTrusted(project, projectService);

    final MessageBusConnection connection = project.getMessageBus().connect();
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
      @Override
      public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
        CIAidProjectService.executeOnThreadPool(project, () -> projectService.processOpenedFile(file));
      }
    });
    Disposer.register(DisposerService.getInstance(project), projectService);
    return null;
  }

  private static void initializeWhenTrusted(Project project, CIAidProjectService projectService) {
    var initializationStarted = new AtomicBoolean();
    Runnable initialize = () -> {
      if (initializationStarted.compareAndSet(false, true)) {
        CIAidProjectService.executeOnThreadPool(project, projectService::afterStartup);
      }
    };

    if (TrustedProjects.isProjectTrusted(project)) {
      initialize.run();
      return;
    }

    var connection = ApplicationManager.getApplication().getMessageBus().connect(project);
    connection.subscribe(TrustedProjectsListener.TOPIC, new TrustedProjectsListener() {
      @Override
      public void onProjectTrusted(@NotNull Project trustedProject) {
        if (trustedProject == project) {
          connection.disconnect();
          initialize.run();
        }
      }
    });
    if (TrustedProjects.isProjectTrusted(project)) {
      connection.disconnect();
      initialize.run();
    }
  }

}
