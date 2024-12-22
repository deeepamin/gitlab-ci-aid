package com.github.deeepamin.gitlabciaid.services;

import com.github.deeepamin.gitlabciaid.model.GitlabCIYamlData;
import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLBlockScalarImpl;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGES;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.TOP_LEVEL_KEYWORDS;
import static com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils.GITLAB_CI_DEFAULT_YAML_FILES;

@Service(Service.Level.PROJECT)
public final class GitlabCIYamlProjectService implements DumbAware, Disposable {
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlProjectService.class);
  private final Map<VirtualFile, GitlabCIYamlData> pluginData;

  public GitlabCIYamlProjectService(Project project) {
    pluginData = new ConcurrentHashMap<>();
  }

  public static GitlabCIYamlProjectService getInstance(Project project) {
    return project.getService(GitlabCIYamlProjectService.class);
  }

  public Map<VirtualFile, GitlabCIYamlData> getPluginData() {
    return pluginData;
  }

  public void clearPluginData() {
    pluginData.clear();
  }

  public void processOpenedFile(Project project, VirtualFile file) {
    if (!GitlabCIYamlUtils.isValidGitlabCIYamlFile(file)) {
      return;
    }
    readGitlabCIYamlData(project, file);
  }

  public void readGitlabCIYamlData(Project project, VirtualFile file) {
    if (pluginData.containsKey(file) && pluginData.get(file).isUpToDate(file)) {
      return;
    }
    var gitlabCIYamlData = new GitlabCIYamlData(file, file.getModificationStamp());
    getGitlabCIYamlData(project, file, gitlabCIYamlData);
  }

  private void getGitlabCIYamlData(Project project, VirtualFile file, GitlabCIYamlData gitlabCIYamlData) {
    parseGitlabCIYamlData(project, file, gitlabCIYamlData);
    pluginData.put(file, gitlabCIYamlData);
    gitlabCIYamlData.getIncludedYamls().forEach(yaml -> {
      var sanitizedYamlPath = FileUtils.sanitizeFilePath(yaml);
      var yamlVirtualFile = FileUtils.getVirtualFile(sanitizedYamlPath, project).orElse(null);
      if (yamlVirtualFile == null) {
        LOG.debug(yaml + " not found on " + project.getBasePath());
        return;
      }
      if (!pluginData.containsKey(yamlVirtualFile)) {
        var includedYamlData = new GitlabCIYamlData(yamlVirtualFile, yamlVirtualFile.getModificationStamp());
        getGitlabCIYamlData(project, yamlVirtualFile, includedYamlData);
      }
    });
  }


  public void parseGitlabCIYamlData(final Project project, final VirtualFile file, final GitlabCIYamlData gitlabCIYamlData) {
    ApplicationManager.getApplication().runReadAction(() -> {
      var psiManager = PsiManager.getInstance(project);
      var psiFile = psiManager.findFile(file);
      if (psiFile == null) {
        LOG.warn("Cannot find gitlab CI yaml file: " + file.getPath());
        return;
      }
      psiFile.accept(new YamlRecursivePsiElementVisitor() {
        @Override
        public void visitFile(@NotNull PsiFile file) {
          var isYamlFile = GitlabCIYamlUtils.isValidGitlabCIYamlFile(file.getVirtualFile());
          if (isYamlFile && file instanceof YAMLFile yamlFile) {
            var topLevelKeys = YAMLUtil.getTopLevelKeys(yamlFile);

            topLevelKeys.forEach(topLevelKey -> {
              // rules can also be top level elements, but they don't have stage as child
              var hasChildStage = PsiUtils.hasChild(topLevelKey, STAGE);
              if (!TOP_LEVEL_KEYWORDS.contains(topLevelKey.getKeyText()) && hasChildStage) {
                // this means it's a job
                gitlabCIYamlData.addJob(topLevelKey);
              }
            });
            super.visitFile(file);
          }
        }

        @Override
        public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
          var keyText = keyValue.getKeyText();
          if (INCLUDE.equals(keyText)) {
            // process include files to add to schema files which have names other than standard name
            var plainTextChildren = PsiUtils.findChildren(keyValue, YAMLPlainTextImpl.class);
            var quotedTextChildren = PsiUtils.findChildren(keyValue, YAMLQuotedText.class);

            plainTextChildren.stream()
                    .map(YAMLBlockScalarImpl::getText)
                    .distinct()
                    .forEach(schemaFile -> {
                      GitlabCIYamlUtils.addYamlFile(schemaFile);
                      gitlabCIYamlData.addIncludedYaml(schemaFile);
                    });
            quotedTextChildren.stream()
                    .map(YAMLQuotedText::getText)
                    .distinct()
                    .forEach(schemaFile -> {
                      GitlabCIYamlUtils.addYamlFile(schemaFile);
                      gitlabCIYamlData.addIncludedYaml(schemaFile);
                    });
          }
          if (STAGE.equals(keyText)) {
            gitlabCIYamlData.addStage(keyValue);
          }
          if (STAGES.equals(keyText)) {
            gitlabCIYamlData.setStagesElement(keyValue);
          }
          super.visitKeyValue(keyValue);
        }
      });
    });
  }

  public List<String> getJobNames() {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getJobs().keySet().stream())
            .toList();
  }

  public List<String> getStageNamesDefinedAtStagesLevel() {
    var stagesElements = pluginData.values().stream()
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

  public List<String> getStageNamesDefinedAtJobLevel () {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getStages().keySet().stream())
            .toList();
  }

  public String getFileName(Project project, Predicate<Map.Entry<VirtualFile, GitlabCIYamlData>> predicate) {
     String filePath = pluginData.entrySet().stream()
             .filter(predicate)
             .map(Map.Entry::getKey)
             .findFirst()
             .map(VirtualFile::getPath)
             .orElse(null);
    var basePath = project.getBasePath();
    if (filePath != null && basePath != null) {
      return filePath.replaceFirst("^" + basePath + "/", "");
    }
    return "";
  }

  public void afterStartup(@NotNull Project project) {
    final var basePath = project.getBasePath();
    var foundDefaultGitlabCIYaml = false;
    for (var yamlFile : GITLAB_CI_DEFAULT_YAML_FILES) {
      final var gitlabCIYamlPath = basePath + File.separator + yamlFile;
      final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(gitlabCIYamlPath);
      if (gitlabCIYamlFile != null) {
        LOG.info("Found " + yamlFile + " in " + gitlabCIYamlPath);
        readGitlabCIYamlData(project, gitlabCIYamlFile);
        foundDefaultGitlabCIYaml = true;
        break;
      }
    }
    if (!foundDefaultGitlabCIYaml) {
      final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
      Arrays.stream(fileEditorManager.getOpenFiles()).forEach(openedFile -> processOpenedFile(project, openedFile));
    }
  }

  @Override
  public void dispose() {
    clearPluginData();
  }
}
