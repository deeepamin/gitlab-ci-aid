package com.github.deeepamin.gitlabciaid.services;

import com.github.deeepamin.gitlabciaid.model.PluginData;
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
  public static final Map<String, PluginData> PLUGIN_DATA = new HashMap<>();

  public static void readPluginData(Project project, VirtualFile file) {
    var pluginData = new PluginData(file.getPath());
    getPluginData(project, file, pluginData);
  }

  private static void getPluginData(Project project, VirtualFile file, PluginData pluginData) {
    parseGitlabCIYamlData(project, file, pluginData);
    PLUGIN_DATA.put(file.getPath(), pluginData);
    pluginData.getIncludedYamls().forEach(yaml -> {
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
        var includedYamlPluginData = new PluginData(yaml);
        getPluginData(project, includedYamlVirtualFile, includedYamlPluginData);
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
            .map(PluginData::getStagesElement)
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

  public static String getFileName(String job, Project project, Predicate<Map.Entry<String, PluginData>> predicate) {
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
