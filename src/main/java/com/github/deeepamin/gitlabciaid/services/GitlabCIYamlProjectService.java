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
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLBlockScalarImpl;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGES;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.TOP_LEVEL_KEYWORDS;

@Service(Service.Level.PROJECT)
public final class GitlabCIYamlProjectService implements DumbAware, Disposable {
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlProjectService.class);
  private final Map<String, GitlabCIYamlData> pluginData = new HashMap<>();

  public static GitlabCIYamlProjectService getInstance(Project project) {
    return project.getService(GitlabCIYamlProjectService.class);
  }

  public Map<String, GitlabCIYamlData> getPluginData() {
    return pluginData;
  }

  public void clearPluginData() {
    pluginData.clear();
  }

  public void processOpenedFile(Project project, VirtualFile file) {
      if (!GitlabCIYamlUtils.isValidGitlabCIYamlFile(file)) {
        return;
      }
      //TODO check for already parsed files, need PSI listeners to track changes in those files
      readGitlabCIYamlData(project, file);
  }

  public void readGitlabCIYamlData(Project project, VirtualFile file) {
    var gitlabCIYamlData = new GitlabCIYamlData(file.getPath());
    getGitlabCIYamlData(project, file, gitlabCIYamlData);
  }

  private void getGitlabCIYamlData(Project project, VirtualFile file, GitlabCIYamlData gitlabCIYamlData) {
    parseGitlabCIYamlData(project, file, gitlabCIYamlData);
    pluginData.put(file.getPath(), gitlabCIYamlData);
    gitlabCIYamlData.getIncludedYamls().forEach(yaml -> {
      if (!pluginData.containsKey(yaml)) {
        var yamlVirtualFile = FileUtils.getVirtualFile(yaml, project).orElse(null);
        if (yamlVirtualFile == null) {
          LOG.debug(yaml + " not found on " + project.getBasePath());
          return;
        }
        var includedYamlData = new GitlabCIYamlData(yaml);
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
          var superParent = keyValue.getParent().getParent();
          if (superParent instanceof YAMLDocument) {
            // top level elements
            var key = keyValue.getKey();
            if (key instanceof LeafPsiElement) {
              key = key.getParent();
            }

            // rules can also be top level elements, but they don't have stage as child
            var hasChildStage = PsiUtils.hasChild(key, STAGE);
            if (!TOP_LEVEL_KEYWORDS.contains(keyText) && hasChildStage) {
              // this means it's a job
              gitlabCIYamlData.addJob(keyValue);
            }
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

  public String getFileName(Project project, Predicate<Map.Entry<String, GitlabCIYamlData>> predicate) {
     String filePath = pluginData.entrySet().stream()
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

  public void afterStartup(@NotNull Project project) {
    final var basePath = project.getBasePath();
    final var gitlabCIYamlPath = basePath + GitlabCIYamlUtils.GITLAB_CI_DEFAULT_YAML_FILE;
    final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(gitlabCIYamlPath);
    if (gitlabCIYamlFile != null) {
      LOG.info("Found " + GitlabCIYamlUtils.GITLAB_CI_DEFAULT_YAML_FILE + " in " + gitlabCIYamlPath);
      readGitlabCIYamlData(project, gitlabCIYamlFile);
    } else {
      final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
      Arrays.stream(fileEditorManager.getOpenFiles()).forEach(openedFile -> processOpenedFile(project, openedFile));
    }
  }

  @Override
  public void dispose() {
    clearPluginData();
  }
}
