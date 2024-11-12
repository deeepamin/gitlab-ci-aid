package com.github.deeepamin.gitlabciaid.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GitlabCIYamlPostStartup implements ProjectActivity {
  @Override
  public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
    final var projectService = GitlabCIYamlProjectService.getInstance(project);
    executeOnThreadPool(() -> projectService.afterStartup(project));

    final MessageBusConnection connection = project.getMessageBus().connect();
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
      @Override
      public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
        executeOnThreadPool(() -> projectService.processOpenedFile(project, file));
      }
    });
    return null;
  }

  public static void executeOnThreadPool(final Runnable runnable) {
    ApplicationManager.getApplication().executeOnPooledThread(runnable);
  }
}
