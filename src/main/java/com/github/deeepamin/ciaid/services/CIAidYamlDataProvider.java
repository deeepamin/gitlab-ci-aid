package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.cache.providers.AbstractIncludeProvider;
import com.github.deeepamin.ciaid.cache.providers.ComponentIncludeProvider;
import com.github.deeepamin.ciaid.cache.providers.LocalIncludeProvider;
import com.github.deeepamin.ciaid.cache.providers.ProjectFileIncludeProvider;
import com.github.deeepamin.ciaid.cache.providers.RemoteUrlIncludeProvider;
import com.github.deeepamin.ciaid.cache.providers.TemplateIncludeProvider;
import com.github.deeepamin.ciaid.model.CIAidYamlData;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeProject;
import com.github.deeepamin.ciaid.model.gitlab.inputs.Input;
import com.github.deeepamin.ciaid.model.gitlab.inputs.InputType;
import com.github.deeepamin.ciaid.parser.CIAidGitLabYamlParser;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.impl.YAMLArrayImpl;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import java.io.File;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class CIAidYamlDataProvider {
  private static final Logger LOG = Logger.getInstance(CIAidYamlDataProvider.class);
  private final Project project;
  private final Map<VirtualFile, CIAidYamlData> pluginData;

  public CIAidYamlDataProvider(Project project) {
    this.project = project;
    pluginData = new ConcurrentHashMap<>();

  }

  public void readGitlabCIYamlData(VirtualFile file, boolean userMarked, boolean forceRead) {
    if (!forceRead && pluginData.containsKey(file) && pluginData.get(file).isUpToDate(file)) {
      return;
    }
    ApplicationManager.getApplication().runReadAction(() -> {
      if (userMarked) {
        GitlabCIYamlUtils.markAsUserCIYamlFile(file, project);
      } else {
        GitlabCIYamlUtils.markAsCIYamlFile(file);
      }
      var parser = new CIAidGitLabYamlParser(project);
      var ciAidYamlData = parser.parseGitlabCIYamlData(file);
      pluginData.put(file, ciAidYamlData);
      readIncludes(ciAidYamlData, userMarked);
    });
  }

  public Map<VirtualFile, CIAidYamlData> getPluginData() {
    return pluginData;
  }

  public void clearPluginData() {
    pluginData.clear();
  }

  public List<String> getJobNames() {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getJobElements().stream())
            .map(jobKeyValue -> handleQuotedText(jobKeyValue.getKeyText()))
            .toList();
  }

  public List<String> getStageNamesDefinedAtStagesLevel() {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getStagesItemElements().stream())
            .map(stage -> handleQuotedText(stage.getText()))
            .toList();
  }

  public List<String> getStageNamesDefinedAtJobLevel () {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getJobStageElements().stream())
            .map(jobStage -> handleQuotedText(jobStage.getText()))
            .distinct()
            .toList();
  }

  public List<Input> getInputs() {
    return pluginData.values().stream()
            .flatMap(yamlData -> yamlData.getInputs().stream())
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
                      case DEFAULT -> {
                        var value = inputKeyValue.getValue();
                        var arrayChildren = PsiUtils.findChildren(value, YAMLArrayImpl.class);
                        if (!arrayChildren.isEmpty()) {
                          defaultValue = arrayChildren.stream()
                                  .map(YAMLArrayImpl::getText)
                                  .collect(Collectors.joining(", "));
                        } else if (value instanceof YAMLScalar yamlScalar) {
                          defaultValue = handleQuotedText(yamlScalar.getText());
                        }
                      }
                      case DESCRIPTION -> inputDescription = handleQuotedText(inputKeyValue.getValueText());
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

  public String getFileName(Predicate<Map.Entry<VirtualFile, CIAidYamlData>> predicate) {
     String filePath = pluginData.entrySet().stream()
             .filter(predicate)
             .map(Map.Entry::getKey)
             .findFirst()
             .map(VirtualFile::getPath)
             .orElse(null);
    var basePath = project.getBasePath();
    if (filePath != null && basePath != null) {
      try {
        return filePath.replaceFirst("^" + basePath + File.separator, "");
      } catch (PatternSyntaxException e) {
        LOG.debug("Regex pattern syntax error in file name: " + e);
      }
    }
    return "";
  }

  private void readIncludes(CIAidYamlData ciAidYamlData, boolean userMarked) {
    ciAidYamlData.getIncludes()
            .forEach(include -> {
              var includeType = include.getFileType();
              AbstractIncludeProvider includeProvider = null;
              switch (includeType) {
                case LOCAL -> includeProvider = new LocalIncludeProvider(project, include.getPath(), userMarked);
                case PROJECT -> {
                  IncludeProject includeProject = (IncludeProject) include;
                  var projectName = includeProject.getProject();
                  var refName = includeProject.getRef();
                  var file = includeProject.getPath();
                  includeProvider = new ProjectFileIncludeProvider(project, file, projectName, refName);
                }
                case REMOTE -> includeProvider = new RemoteUrlIncludeProvider(project, include.getPath());
                case TEMPLATE -> includeProvider = new TemplateIncludeProvider(project, include.getPath());
                case COMPONENT -> includeProvider = new ComponentIncludeProvider(project, include.getPath());
              }
              if (includeProvider != null) {
                CIAidProjectService.executeOnThreadPool(includeProvider::readIncludeFile);
              }
            });
  }

  public @NotNull Map<String, List<VirtualFile>> getVariableAndContainingFiles(CIAidProjectService ciAidProjectService) {
    return ciAidProjectService.getPluginData()
            .entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().getVariables().stream()
                    .map(YAMLKeyValue::getKeyText)
                    .map(variable -> new AbstractMap.SimpleEntry<>(variable, entry.getKey()))
            )
            .collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ));
  }

  public String getStagesItemFileName(String stagesItem) {
    return getFileName(
            (entry) -> entry.getValue().getStagesItemElements()
                    .stream()
                    .anyMatch(stage -> handleQuotedText(stage.getText()).equals(stagesItem)));
  }

  public String getJobFileName(String job) {
    return getFileName(
            (entry) -> entry.getValue().getJobElements()
            .stream()
            .map(jobKeyValue -> handleQuotedText(jobKeyValue.getKeyText()))
            .anyMatch(jobText -> jobText.equals(job)));
  }

  public String getJobStageFileName(String stage) {
    return getFileName(
            (entry) -> entry.getValue().getJobStageElements()
                    .stream()
                    .anyMatch(jobStage -> handleQuotedText(jobStage.getText()).equals(stage)));
  }
}
