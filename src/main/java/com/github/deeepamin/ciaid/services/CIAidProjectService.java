package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.model.CIAidYamlData;
import com.github.deeepamin.ciaid.model.gitlab.Input;
import com.github.deeepamin.ciaid.model.gitlab.InputType;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;
import org.jetbrains.yaml.psi.impl.YAMLBlockScalarImpl;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.ARRAY;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.BOOLEAN;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.DEFAULT;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.DESCRIPTION;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.INPUTS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.NUMBER;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.SPEC;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.STAGES;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.STRING;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.TOP_LEVEL_KEYWORDS;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.TYPE;
import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.VARIABLES;
import static com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils.GITLAB_CI_DEFAULT_YAML_FILES;
import static com.github.deeepamin.ciaid.utils.ReferenceUtils.handleQuotedText;

@Service(Service.Level.PROJECT)
public final class CIAidProjectService implements DumbAware, Disposable {
  private static final Logger LOG = Logger.getInstance(CIAidProjectService.class);
  private final Map<VirtualFile, CIAidYamlData> pluginData;

  public CIAidProjectService() {
    pluginData = new ConcurrentHashMap<>();
  }

  public static CIAidProjectService getInstance(Project project) {
    return project.getService(CIAidProjectService.class);
  }

  public Map<VirtualFile, CIAidYamlData> getPluginData() {
    return pluginData;
  }

  public void clearPluginData() {
    pluginData.clear();
  }

  public void processOpenedFile(Project project, VirtualFile file) {
    if (!GitlabCIYamlUtils.isValidGitlabCIYamlFile(file)) {
      return;
    }
    readGitlabCIYamlData(project, file, GitlabCIYamlUtils.isMarkedAsUserCIYamlFile(file));
  }

  public void readGitlabCIYamlData(Project project, VirtualFile file, boolean userMarked) {
    if (pluginData.containsKey(file) && pluginData.get(file).isUpToDate(file)) {
      return;
    }
    var gitlabCIYamlData = new CIAidYamlData(file, file.getModificationStamp());
    getGitlabCIYamlData(project, file, gitlabCIYamlData, userMarked);
  }

  private void getGitlabCIYamlData(Project project, VirtualFile file, CIAidYamlData CIAidYamlData, boolean userMarked) {
    if (userMarked) {
      GitlabCIYamlUtils.markAsUserCIYamlFile(file, project);
    } else {
      GitlabCIYamlUtils.markAsCIYamlFile(file);
    }
    parseGitlabCIYamlData(project, file, CIAidYamlData);
    pluginData.put(file, CIAidYamlData);
    CIAidYamlData.getIncludedYamls().forEach(yaml -> {
      var sanitizedYamlPath = FileUtils.sanitizeFilePath(yaml);
      var yamlVirtualFile = FileUtils.getVirtualFile(sanitizedYamlPath, project).orElse(null);
      if (yamlVirtualFile == null) {
        LOG.debug(yaml + " not found on " + project.getBasePath());
        return;
      }
      if (!pluginData.containsKey(yamlVirtualFile)) {
        var includedYamlData = new CIAidYamlData(yamlVirtualFile, yamlVirtualFile.getModificationStamp());
        getGitlabCIYamlData(project, yamlVirtualFile, includedYamlData, userMarked);
      }
    });
  }


  public void parseGitlabCIYamlData(final Project project, final VirtualFile file, final CIAidYamlData ciAidYamlData) {
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
            var topLevelKeys = YamlUtils.getTopLevelKeysForAllDocuments(yamlFile);

            topLevelKeys.forEach(topLevelKey -> {
              if (!TOP_LEVEL_KEYWORDS.contains(topLevelKey.getKeyText())) {
                // this means it's a job
                ciAidYamlData.addJob(topLevelKey);
              }
            });
            super.visitFile(file);
          }
        }

        @Override
        public void visitScalar(@NotNull YAMLScalar scalar) {
          if (PsiUtils.isNotSpecInputsElement(scalar)) {
            if (YamlUtils.isYamlTextElement(scalar)) {
              var isChildOfStagesElement = PsiUtils.isChild(scalar, List.of(STAGES));
              if (isChildOfStagesElement) {
                ciAidYamlData.addStagesItem(scalar);
              }
            }
          }
          super.visitScalar(scalar);
        }

        @Override
        public void visitValue(@NotNull YAMLValue value) {
          if (PsiUtils.isNotSpecInputsElement(value)) {
            var isChildOfStageElement = PsiUtils.isChild(value, List.of(STAGE));
            if (isChildOfStageElement) {
              ciAidYamlData.addJobStage(value);
            }
          }
          super.visitValue(value);
        }

        @Override
        public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
          var keyText = keyValue.getKeyText();
          switch (keyText) {
            case INCLUDE -> {
              if (PsiUtils.isNotSpecInputsElement(keyValue)) {
                // process include files to add to schema files which have names other than standard name
                var plainTextChildren = PsiUtils.findChildren(keyValue, YAMLPlainTextImpl.class);
                var quotedTextChildren = PsiUtils.findChildren(keyValue, YAMLQuotedText.class);

                plainTextChildren.stream()
                        .map(YAMLBlockScalarImpl::getText)
                        .distinct()
                        .forEach(ciAidYamlData::addIncludedYaml);
                quotedTextChildren.stream()
                        .map(YAMLQuotedText::getText)
                        .distinct()
                        .forEach(ciAidYamlData::addIncludedYaml);
              }
            }
            case INPUTS -> {
              boolean isSpecInputsElement = PsiUtils.findParent(keyValue, List.of(SPEC)).isPresent();
              if (isSpecInputsElement) {
                var value = keyValue.getValue();
                if (value instanceof YAMLBlockMappingImpl blockMapping) {
                  blockMapping.getKeyValues()
                          .forEach(ciAidYamlData::addInput);
                }
              }
            }
            case VARIABLES -> {
              if (PsiUtils.isNotSpecInputsElement(keyValue)) {
                var value = keyValue.getValue();
                if (value instanceof YAMLBlockMappingImpl blockMapping) {
                  blockMapping.getKeyValues()
                          .forEach(ciAidYamlData::addVariable);
                }
              }
            }
          }
        super.visitKeyValue(keyValue);
        }
      });
    });
  }

  public List<String> getJobNames() {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getJobElements().stream())
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(pointer -> handleQuotedText(pointer.getElement().getKeyText()))
            .toList();
  }

  public List<String> getStageNamesDefinedAtStagesLevel() {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getStagesItemElements().stream())
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(pointer -> handleQuotedText(pointer.getElement().getText()))
            .toList();
  }

  public List<String> getStageNamesDefinedAtJobLevel () {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getJobStageElements().stream())
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(pointer -> handleQuotedText(pointer.getElement().getText()))
            .distinct()
            .toList();
  }

  public List<Input> getInputs() {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getInputs().stream())
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(SmartPsiElementPointer::getElement)
            .map(inputsKeyValue -> {
                var inputName = inputsKeyValue.getKeyText();
                var inputValue = inputsKeyValue.getValue();
                String inputDescription = "";
                String defaultValue = "";
                var inputType = InputType.STRING;
                if (inputValue instanceof YAMLBlockMappingImpl blockMappingInput) {
                  for (YAMLKeyValue inputKeyValue : blockMappingInput.getKeyValues()) {
                    switch (inputKeyValue.getKeyText()) {
                      case TYPE -> {
                        var valueText = inputKeyValue.getValueText();
                        inputType = switch (valueText) {
                          case STRING -> InputType.STRING;
                          case BOOLEAN -> InputType.BOOLEAN;
                          case NUMBER -> InputType.NUMBER;
                          case ARRAY -> InputType.ARRAY;
                          default -> inputType;
                        };
                      }
                      case DEFAULT -> defaultValue = inputKeyValue.getValueText();
                      case DESCRIPTION -> inputDescription = inputKeyValue.getValueText();
                    }
                  }
                  SmartPointerManager pointerManager = SmartPointerManager.getInstance(inputsKeyValue.getProject());
                  SmartPsiElementPointer<YAMLKeyValue> inputPointer = pointerManager.createSmartPsiElementPointer(inputsKeyValue);
                  return new Input(inputName, inputDescription, defaultValue, inputType, inputPointer);
                }
              return null;
            })
            .toList();
  }

  public String getFileName(Project project, Predicate<Map.Entry<VirtualFile, CIAidYamlData>> predicate) {
     String filePath = pluginData.entrySet().stream()
             .filter(predicate)
             .map(Map.Entry::getKey)
             .findFirst()
             .map(VirtualFile::getPath)
             .orElse(null);
    var basePath = project.getBasePath();
    if (filePath != null && basePath != null) {
      return filePath.replaceFirst("^" + basePath + File.separator, "");
    }
    return "";
  }

  public String getJobFileName(Project project, String job) {
    return getFileName(project,
            (entry) -> entry.getValue().getJobElements()
            .stream()
            .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
            .map(pointer -> handleQuotedText(pointer.getElement().getKeyText()))
            .anyMatch(pointerText -> pointerText.equals(job)));
  }

  public String getJobStageFileName(Project project, String stage) {
    return getFileName(project,
            (entry) -> entry.getValue().getJobStageElements()
                    .stream()
                    .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
                    .map(pointer -> handleQuotedText(pointer.getElement().getText()))
                    .anyMatch(pointerText -> pointerText.equals(stage)));
  }

  public String getStagesItemFileName(Project project, String stagesItem) {
    return getFileName(project,
            (entry) -> entry.getValue().getStagesItemElements()
                    .stream()
                    .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
                    .map(pointer -> handleQuotedText(pointer.getElement().getText()))
                    .anyMatch(pointerText -> pointerText.equals(stagesItem)));
  }

  public void afterStartup(@NotNull Project project) {
    readDefaultGitlabCIYaml(project);
    readUserMarkedYamls(project);
  }

  private void readDefaultGitlabCIYaml(@NotNull Project project) {
    final var ciAidSettingsState = CIAidSettingsState.getInstance(project);
    if (!ciAidSettingsState.defaultGitlabCIYamlPath.isBlank()) {
      final var defaultGitlabCIYamlPath = ciAidSettingsState.defaultGitlabCIYamlPath;
      final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(defaultGitlabCIYamlPath);
      if (gitlabCIYamlFile != null) {
        readGitlabCIYamlData(project, gitlabCIYamlFile, false);
        return;
      }
    }
    // if default yaml path is not set, check for default gitlab ci yaml files
    final var basePath = project.getBasePath();
    for (var yamlFile : GITLAB_CI_DEFAULT_YAML_FILES) {
      final var gitlabCIYamlPath = basePath + File.separator + yamlFile;
      final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(gitlabCIYamlPath);
      if (gitlabCIYamlFile != null) {
        LOG.info("Found " + yamlFile + " in " + gitlabCIYamlPath);
        readGitlabCIYamlData(project, gitlabCIYamlFile, false);
        break;
      }
    }
  }

  private void readUserMarkedYamls(@NotNull Project project) {
    final var ciAidSettingsState = CIAidSettingsState.getInstance(project);
    ciAidSettingsState.yamlToUserMarkings.forEach((path, ignore) -> {
      final var gitlabCIYamlFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (gitlabCIYamlFile != null) {
        if (ignore) {
          GitlabCIYamlUtils.ignoreCIYamlFile(gitlabCIYamlFile, project);
          return;
        }
        readGitlabCIYamlData(project, gitlabCIYamlFile, true);
      }
    });
  }

  @Override
  public void dispose() {
    clearPluginData();
  }
}
