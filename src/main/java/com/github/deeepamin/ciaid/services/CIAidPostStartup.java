package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
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

public class CIAidPostStartup implements ProjectActivity {
  @Override
  public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
    final var projectService = CIAidProjectService.getInstance(project);
    CIAidProjectService.executeOnThreadPool(project, () -> {
      CIAidCacheService.getInstance().loadCacheFromDisk(project);
      projectService.afterStartup();
    });

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

}
