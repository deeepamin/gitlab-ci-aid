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
import java.util.regex.Pattern;

public class FileUtils {
  private static final Logger LOG = Logger.getInstance(FileUtils.class);
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

  public static void createFile(Path path) {
    try {
      if (!Files.exists(path)) {
        var parent = path.getParent();
        Files.createDirectories(parent);
        Files.createFile(path);
      }
    } catch (IOException e) {
      LOG.error("Error creating file " + path, e);
    }
  }

  public static void refreshFileAndOpenInEditor(Path path, Project project) {
    var virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(path.toString()));
    if (virtualFile != null) {
      FileEditorManager.getInstance(project).openFile(virtualFile, true);
    }
  }

  public static Path getFilePath(String fileRelativePathToRoot, Project project) {
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
    return Path.of(pathBuilder.toString());
  }

  public static ScriptPathIndex getShOrPyScript(String elementText) {
    String regex = "(\\.?/?\\S+\\.(sh|py))";
    Pattern pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(elementText);
    if (matcher.find()) {
      return new ScriptPathIndex(matcher.group(), matcher.start(), matcher.end());
    }
    return null;
  }

  public record ScriptPathIndex(String path, int start, int end) {
  }
}
