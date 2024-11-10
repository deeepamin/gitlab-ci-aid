package com.github.deeepamin.gitlabciaid.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FileUtils {
  private static final Logger LOG = Logger.getInstance(FileUtils.class);
  public static final List<String> SCRIPT_EXTENSIONS = List.of(".sh", ".py");
  public static final List<String> SCRIPT_RUNNERS = List.of("python ", "python3 ");

  public static Optional<VirtualFile> getVirtualFile(String fileRelativePathToRoot, Project project) {
    var basePath = project.getBasePath();
    for (String runner : FileUtils.SCRIPT_RUNNERS) {
      if (fileRelativePathToRoot.startsWith(runner)) {
        fileRelativePathToRoot = fileRelativePathToRoot.substring(runner.length());
      }
    }
    var pathBuilder = new StringBuilder();
    pathBuilder.append(basePath);
    if (!fileRelativePathToRoot.startsWith(File.separator)) {
      pathBuilder.append(File.separator);
    }
    pathBuilder.append(fileRelativePathToRoot);
    var virtualFile = LocalFileSystem.getInstance().findFileByPath(pathBuilder.toString());
    if (virtualFile != null && virtualFile.exists() && !virtualFile.isDirectory() && virtualFile.isValid()) {
      return Optional.of(virtualFile);
    }
    return Optional.empty();
  }

  public static boolean createFile(String fileRelativePathToRoot, Project project, boolean openAfterCreation) {
    var basePath = project.getBasePath();
    var pathBuilder = new StringBuilder();
    pathBuilder.append(basePath);
    for (String runner : FileUtils.SCRIPT_RUNNERS) {
      if (fileRelativePathToRoot.startsWith(runner)) {
        fileRelativePathToRoot = fileRelativePathToRoot.substring(runner.length());
      }
    }
    if (!fileRelativePathToRoot.startsWith(File.separator)) {
      pathBuilder.append(File.separator);
    }
    pathBuilder.append(fileRelativePathToRoot);
    var path = Path.of(pathBuilder.toString());
    try {
      if (!Files.exists(path)) {
        Files.createFile(path);
      }
      if (openAfterCreation) {
        var virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(path.toString()));
        if (virtualFile != null) {
          FileEditorManager.getInstance(project).openFile(virtualFile, true);
        }
      }
    } catch (IOException e) {
      LOG.error("Error creating file", e);
    }
    return Files.exists(path);
  }
}
