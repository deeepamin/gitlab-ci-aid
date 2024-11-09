package com.github.deeepamin.gitlabciaid.services;

import com.github.deeepamin.gitlabciaid.model.GitlabCIYamlData;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils.parseGitlabCIYamlData;

public class GitlabCIYamlCache {
  public static final Map<String, GitlabCIYamlData> PLUGIN_DATA = new HashMap<>();

  public static void readGitlabCIYamlData(Project project, VirtualFile file) {
    var gitlabCIYamlData = new GitlabCIYamlData(file.getPath());
    getGitlabCIYamlData(project, file, gitlabCIYamlData);
  }

  private static void getGitlabCIYamlData(Project project, VirtualFile file, GitlabCIYamlData gitlabCIYamlData) {
    parseGitlabCIYamlData(project, file, gitlabCIYamlData);
    PLUGIN_DATA.put(file.getPath(), gitlabCIYamlData);
    gitlabCIYamlData.getIncludedYamls().forEach(yaml -> {
      if (!PLUGIN_DATA.containsKey(yaml)) {
        var pathBuilder = new StringBuilder();
        var basePath = project.getBasePath();
        pathBuilder.append(basePath);
        if (!yaml.startsWith(File.separator)) {
          pathBuilder.append(File.separator);
        }
        pathBuilder.append(yaml);
        var includedYamlVirtualFile = LocalFileSystem.getInstance().findFileByPath(pathBuilder.toString());
        if (includedYamlVirtualFile == null || !includedYamlVirtualFile.exists() || includedYamlVirtualFile.isDirectory() || !includedYamlVirtualFile.isValid()) {
          return;
        }
        var includedYamlData = new GitlabCIYamlData(yaml);
        getGitlabCIYamlData(project, includedYamlVirtualFile, includedYamlData);
      }
    });
  }

  public static List<String> getJobNames() {
    return PLUGIN_DATA.values().stream()
            .flatMap(yamlData -> yamlData.getJobs().keySet().stream())
            .toList();
  }

  public static List<String> getStageNamesDefinedAtStagesLevel() {
    var stagesElements = PLUGIN_DATA.values().stream()
            .map(GitlabCIYamlData::getStagesElement)
            .toList();

    List<String> definedStages = new ArrayList<>();
    for (var stageElement : stagesElements) {
      PsiUtils.findChildren(stageElement, YAMLPlainTextImpl.class).stream()
              .map(YAMLPlainTextImpl::getText)
              .forEach(definedStages::add);
    }
    return definedStages;
  }

  public static List<String> getStageNamesDefinedAtJobLevel () {
    return PLUGIN_DATA.values().stream()
            .flatMap(yamlData -> yamlData.getStages().keySet().stream())
            .toList();
  }

  public static String getFileName(Project project, Predicate<Map.Entry<String, GitlabCIYamlData>> predicate) {
     String filePath = PLUGIN_DATA.entrySet().stream()
             .filter(predicate)
             .map(Map.Entry::getKey)
             .findFirst()
             .orElse(null);
    var basePath = project.getBasePath();
    if (filePath != null && basePath != null) {
      return filePath.replaceFirst("^" + basePath + "/", "");
    }
    return "";
  }
}
