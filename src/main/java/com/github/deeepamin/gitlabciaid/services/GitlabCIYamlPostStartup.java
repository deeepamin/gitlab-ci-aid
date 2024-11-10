package com.github.deeepamin.gitlabciaid.services;

import com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class GitlabCIYamlPostStartup implements ProjectActivity {

  private static void processOpenedFile(Project project, VirtualFile file) {
    executeOnThreadPool(project, () -> {
      if (!GitlabCIYamlUtils.isValidGitlabCIYamlFile(file)) {
        return;
      }
      var applicationService = GitlabCIYamlApplicationService.getInstance();
      applicationService.readGitlabCIYamlData(project, file);
    });
  }

  public static void executeOnThreadPool(final Project project, final Runnable runnable) {
    if (DumbService.isDumb(project)) {
      DumbService.getInstance(project).runWhenSmart(() -> ApplicationManager.getApplication().executeOnPooledThread(runnable));
    } else {
      ApplicationManager.getApplication().executeOnPooledThread(runnable);
    }
  }

  @Override
  public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
    final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
    Arrays.stream(fileEditorManager.getOpenFiles()).forEach(openedFile -> processOpenedFile(project, openedFile));

    final MessageBusConnection connection = project.getMessageBus().connect();
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
      @Override
      public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
        processOpenedFile(project, file);
      }
    });
    return null;
  }
}
