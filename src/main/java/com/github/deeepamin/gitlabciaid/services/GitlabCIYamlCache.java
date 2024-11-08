package com.github.deeepamin.gitlabciaid.services;

import com.github.deeepamin.gitlabciaid.model.PluginData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
}
