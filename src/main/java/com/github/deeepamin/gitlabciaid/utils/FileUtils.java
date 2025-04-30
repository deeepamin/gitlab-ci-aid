package com.github.deeepamin.gitlabciaid.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class FileUtils {
  private static final Logger LOG = Logger.getInstance(FileUtils.class);

  public static Optional<VirtualFile> getVirtualFile(String fileRelativePathToRoot, Project project) {
    var basePath = project.getBasePath();
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

  public static Optional<VirtualFile> findVirtualFile(final String filePath, Project project) {
    if (filePath == null || filePath.isEmpty()) {
      return Optional.empty();
    }
    String fileName = filePath;
    if (filePath.startsWith("./")) {
      fileName = fileName.substring(2);
    }
    if (filePath.contains(File.separator)) {
      fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
    }
    return FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.projectScope(project))
            .stream()
            .filter(virtualFile -> {
              var absolutePath = virtualFile.getPath();
              var filePathToCheck = filePath;
              filePathToCheck = FileUtils.sanitizeFilePath(filePathToCheck);
              if (filePathToCheck.startsWith("./")) {
                filePathToCheck = filePathToCheck.substring(2);
              }
              return absolutePath.contains(filePathToCheck);
            })
            .findFirst();
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

  public static Path getFilePath(String textContainingFilePath, Project project) throws InvalidPathException {
    var basePath = project.getBasePath();
    var filePathIndexes = getFilePathAndIndexes(textContainingFilePath);
    if (filePathIndexes.isEmpty()) {
      return null;
    }
    var filePathIndex = filePathIndexes.getFirst();
    var pathBuilder = new StringBuilder();
    pathBuilder.append(basePath);
    if (!textContainingFilePath.startsWith(File.separator)) {
      pathBuilder.append(File.separator);
    }
    pathBuilder.append(filePathIndex.path().trim());
    return Path.of(pathBuilder.toString());
  }

  public static List<FilePathIndex> getFilePathAndIndexes(String elementText) {
    String regex = "(?:^|\\s)(\\./|/|[\\w\\-./]+)+\\.\\w+(?=\\s|$)";
    Pattern pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(elementText);
    List<FilePathIndex> result = new ArrayList<>();
    while (matcher.find()) {
      result.add(new FilePathIndex(matcher.group().trim(), matcher.start(), matcher.end()));
    }
    return result;
  }

  public static String sanitizeFilePath(String filePath) {
    if (filePath == null) {
      return null;
    }
    if (filePath.startsWith("\"") && filePath.endsWith("\"")) {
      filePath = filePath.replaceAll("\"", "");
    } else if (filePath.startsWith("'") && filePath.endsWith("'")) {
      filePath = filePath.replaceAll("'", "");
    }
    return filePath.trim();
  }

  public record FilePathIndex(String path, int start, int end) {

  }
}
