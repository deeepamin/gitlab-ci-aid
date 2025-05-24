package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.model.CIAidYamlData;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeFile;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeFileType;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeProject;
import com.github.deeepamin.ciaid.model.gitlab.inputs.Input;
import com.github.deeepamin.ciaid.model.gitlab.inputs.InputType;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
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
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import java.io.File;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.ARRAY;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.BOOLEAN;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.COMPONENT;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.DEFAULT;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.DESCRIPTION;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.FILE;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.INPUTS;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.LOCAL;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.NUMBER;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.PROJECT;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.REF;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.REMOTE;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.SPEC;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.STAGES;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.STRING;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.TEMPLATE;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.TOP_LEVEL_KEYWORDS;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.TYPE;
import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.VARIABLES;
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
    doReadGitlabCIYamlData(project, file, gitlabCIYamlData, userMarked);
  }

  private void doReadGitlabCIYamlData(Project project, VirtualFile file, CIAidYamlData ciAidYamlData, boolean userMarked) {
    if (userMarked) {
      GitlabCIYamlUtils.markAsUserCIYamlFile(file, project);
    } else {
      GitlabCIYamlUtils.markAsCIYamlFile(file);
    }
    ApplicationManager.getApplication().runReadAction(() -> {
      parseGitlabCIYamlData(project, file, ciAidYamlData);
      readIncludes(project, ciAidYamlData, userMarked);
    });
  }

  private void readIncludes(Project project, CIAidYamlData ciAidYamlData, boolean userMarked) {
    ciAidYamlData.getIncludes()
            .forEach(include -> {
              var includeType = include.getFileType();
              switch (includeType) {
                case LOCAL -> {
                  var sanitizedYamlPath = FileUtils.sanitizeFilePath(include.getPath());
                  var includeVirtualFile = FileUtils.getVirtualFile(sanitizedYamlPath, project).orElse(null);
                  doReadIncludeVirtualFile(includeVirtualFile, project, ciAidYamlData, userMarked);
                }
                case PROJECT -> {
                  IncludeProject includeProject = (IncludeProject) include;
                  var projectName = includeProject.getProject();
                  var refName = includeProject.getRef();
                  var file = includeProject.getPath();
                  // TODO cache files from project
                }
                case REMOTE -> {
                  // TODO handle remote includes
                }
                case TEMPLATE -> {
                  // TODO handle template includes
                }
                case COMPONENT -> {
                  // TODO handle component includes
                }
              }
            });
  }

  private void doReadIncludeVirtualFile(VirtualFile includeVirtualFile, Project project, CIAidYamlData ciAidYamlData, boolean userMarked) {
    if (includeVirtualFile == null) {
      return;
    }
    if (!pluginData.containsKey(includeVirtualFile)) {
      var includedYamlData = new CIAidYamlData(includeVirtualFile, includeVirtualFile.getModificationStamp());
      doReadGitlabCIYamlData(project, includeVirtualFile, includedYamlData, userMarked);
    }
  }

  public void parseGitlabCIYamlData(final Project project, final VirtualFile file, final CIAidYamlData ciAidYamlData) {
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
            var isChildOfInclude = PsiUtils.isChild(scalar, List.of(INCLUDE));
            var isNotChildOfOtherIncludes = !PsiUtils.isChild(scalar, List.of(LOCAL, REMOTE, TEMPLATE, COMPONENT, PROJECT, REF, FILE));
            if (isChildOfInclude && isNotChildOfOtherIncludes) {
              var include = new IncludeFile();
              var path = handleQuotedText(scalar.getText());
              var isRemote = CIAidUtils.isHttpUrl(path);
              if (isRemote) {
                include.setFileType(IncludeFileType.REMOTE);
              } else {
                include.setFileType(IncludeFileType.LOCAL);
              }
              include.setPath(handleQuotedText(scalar.getText()));
              ciAidYamlData.addInclude(include);
            }
            var isChildOfProjectFileInclude = PsiUtils.isChild(scalar, List.of(FILE));
            if (isChildOfInclude && isChildOfProjectFileInclude) {
              var blockMappingOptional = PsiUtils.findParentOfType(scalar, YAMLBlockMappingImpl.class);
              if (blockMappingOptional.isPresent()) {
                var blockMapping = blockMappingOptional.get();
                var keyValues = blockMapping.getKeyValues();
                var keys = keyValues.stream()
                        .map(YAMLKeyValue::getKeyText)
                        .collect(Collectors.toSet());
                if (keys.contains(PROJECT) && keys.contains(FILE)) {
                  var includeProject = new IncludeProject();
                  includeProject.setFileType(IncludeFileType.PROJECT);
                  keyValues.forEach(keyValue -> {
                    var keyText = keyValue.getKeyText();
                    switch (keyText) {
                      case PROJECT -> includeProject.setProject(keyValue.getValueText());
                      case REF -> includeProject.setRef(keyValue.getValueText());
                      case FILE -> includeProject.setPath(handleQuotedText(scalar.getText()));
                    }
                  });
                  ciAidYamlData.addInclude(includeProject);
                }
              }
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
          case COMPONENT, REMOTE, LOCAL, TEMPLATE -> {
            var isChildOfInclude = PsiUtils.isChild(keyValue, List.of(INCLUDE));
            if (isChildOfInclude) {
              var include = new IncludeFile();
              include.setFileType(IncludeFileType.fromString(keyValue.getKeyText()));
              include.setPath(handleQuotedText(keyValue.getValueText()));
              ciAidYamlData.addInclude(include);
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
    pluginData.put(file, ciAidYamlData);
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

  public @NotNull Map<String, List<VirtualFile>> getVariableAndContainingFiles() {
    return getPluginData()
            .entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().getVariables().stream()
                    .filter(pointer -> pointer.getElement() != null && pointer.getElement().isValid())
                    .map(SmartPsiElementPointer::getElement)
                    .map(YAMLKeyValue::getKeyText)
                    .map(variable -> new AbstractMap.SimpleEntry<>(variable, entry.getKey()))
            )
            .collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ));
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
