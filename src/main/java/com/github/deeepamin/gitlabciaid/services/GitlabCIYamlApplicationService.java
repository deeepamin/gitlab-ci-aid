package com.github.deeepamin.gitlabciaid.services;

import com.github.deeepamin.gitlabciaid.model.GitlabCIYamlData;
import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils.parseGitlabCIYamlData;

public class GitlabCIYamlApplicationService {
  private static final Map<String, GitlabCIYamlData> PLUGIN_DATA = new HashMap<>();

  public static GitlabCIYamlApplicationService getInstance() {
    return ApplicationManager.getApplication().getService(GitlabCIYamlApplicationService.class);
  }

  public static Map<String, GitlabCIYamlData> getPluginData() {
    return PLUGIN_DATA;
  }

  public void readGitlabCIYamlData(Project project, VirtualFile file) {
    var gitlabCIYamlData = new GitlabCIYamlData(file.getPath());
    getGitlabCIYamlData(project, file, gitlabCIYamlData);
  }

  private void getGitlabCIYamlData(Project project, VirtualFile file, GitlabCIYamlData gitlabCIYamlData) {
    parseGitlabCIYamlData(project, file, gitlabCIYamlData);
    PLUGIN_DATA.put(file.getPath(), gitlabCIYamlData);
    gitlabCIYamlData.getIncludedYamls().forEach(yaml -> {
      if (!PLUGIN_DATA.containsKey(yaml)) {
        var yamlVirtualFile = FileUtils.getVirtualFile(yaml, project).orElse(null);
        if (yamlVirtualFile == null) {
          return;
        }
        var includedYamlData = new GitlabCIYamlData(yaml);
        getGitlabCIYamlData(project, yamlVirtualFile, includedYamlData);
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
