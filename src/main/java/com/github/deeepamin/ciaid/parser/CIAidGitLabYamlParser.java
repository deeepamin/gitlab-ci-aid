package com.github.deeepamin.ciaid.parser;

import com.github.deeepamin.ciaid.model.CIAidYamlData;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeFile;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeFileType;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeProject;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLAnchor;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class CIAidGitLabYamlParser {
  private static final Logger LOG = Logger.getInstance(CIAidGitLabYamlParser.class);
  private final Project project;
  private final Set<YAMLBlockMappingImpl> processedProjectIncludes =
          Collections.newSetFromMap(new IdentityHashMap<>());
  private CIAidYamlData ciAidYamlData;

  public CIAidGitLabYamlParser(Project project) {
    this.project = project;
  }

  public CIAidYamlData parseGitlabCIYamlData(final VirtualFile file) {
    return ReadAction.computeBlocking(() -> {
        processedProjectIncludes.clear();
        var psiManager = PsiManager.getInstance(project);
        var psiFile = psiManager.findFile(file);
        if (psiFile == null) {
            LOG.warn("Cannot find gitlab CI yaml file: " + file.getPath());
            return null;
        }
        this.ciAidYamlData = new CIAidYamlData(file, file.getModificationStamp());
        ProgressManager.checkCanceled();

        psiFile.accept(new YamlRecursivePsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                ProgressManager.checkCanceled();
                readFile(file);
                super.visitFile(file);
            }

            @Override
            public void visitAnchor(@NotNull YAMLAnchor anchor) {
                ProgressManager.checkCanceled();
                ciAidYamlData.addAnchor(anchor);
            }

            @Override
            public void visitScalar(@NotNull YAMLScalar scalar) {
                ProgressManager.checkCanceled();
                if (GitlabCIYamlUtils.isNotSpecInputsElement(scalar)) {
                    if (YamlUtils.isYamlTextElement(scalar)) {
                        readStages(scalar);
                        readIncludes(scalar);
                    }
                }
                super.visitScalar(scalar);
            }

            @Override
            public void visitValue(@NotNull YAMLValue value) {
                ProgressManager.checkCanceled();
                if (GitlabCIYamlUtils.isNotSpecInputsElement(value)) {
                    readJobStage(value);
                }
                super.visitValue(value);
            }

            @Override
            public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
                ProgressManager.checkCanceled();
                var keyText = keyValue.getKeyText();
                switch (keyText) {
                    case INPUTS -> readInputs(keyValue);
                    case VARIABLES -> readVariables(keyValue);
                }
                super.visitKeyValue(keyValue);
            }
        });
        return ciAidYamlData;
    });
  }

  // read methods below

  private void readFile(@NotNull PsiFile file) {
    var isYamlFile = CIAidProjectService.isValidGitlabCIYamlFile(file.getVirtualFile());
    if (isYamlFile && file instanceof YAMLFile yamlFile) {
      var topLevelKeys = YamlUtils.getTopLevelKeysForAllDocuments(yamlFile);

      topLevelKeys.forEach(topLevelKey -> {
        ProgressManager.checkCanceled();
        if (!TOP_LEVEL_KEYWORDS.contains(topLevelKey.getKeyText())) {
          // this means it's a job
          ciAidYamlData.addJob(topLevelKey);
        }
      });
    }
  }
  private void readStages(@NotNull YAMLScalar scalar) {
    var isChildOfStagesElement = PsiUtils.isChild(scalar, List.of(STAGES));
    if (isChildOfStagesElement) {
      ciAidYamlData.addStagesItem(scalar);
    }
  }

  private void readIncludes(@NotNull YAMLScalar scalar) {
    var isChildOfInclude = PsiUtils.isChild(scalar, List.of(INCLUDE));
    var isNotChildOfOtherIncludes = !PsiUtils.isChild(scalar, INCLUDE_POSSIBLE_CHILD_KEYWORDS);
    if (isChildOfInclude && isNotChildOfOtherIncludes) {
      // this is a top-level include which means local/remote
      var include = new IncludeFile();
      var path = handleQuotedText(scalar.getText());
      var isRemote = CIAidUtils.isValidUrl(path);
      if (isRemote) {
        include.setFileType(IncludeFileType.REMOTE);
      } else {
        include.setFileType(IncludeFileType.LOCAL);
      }
      include.setPath(path);
      ciAidYamlData.addInclude(include);
    }
    var isChildOfKeyValueIncludes = PsiUtils.isChild(scalar, List.of(LOCAL, REMOTE, TEMPLATE, COMPONENT));
    var isNotChildOfRulesInputsIncludes = !PsiUtils.isChild(scalar, List.of(RULES, INPUTS));
    if (isChildOfInclude && isChildOfKeyValueIncludes && isNotChildOfRulesInputsIncludes) {
      // this is a key-value include which means local/remote/template/component, cache the files and skip rules/inputs
      var yamlKeyValueOptional = PsiUtils.findParentOfType(scalar, YAMLKeyValue.class);
      yamlKeyValueOptional.ifPresent(keyValue -> {
        var keyText = keyValue.getKeyText();
        IncludeFileType includeFileType;
        switch (keyText) {
          case REMOTE -> includeFileType = IncludeFileType.REMOTE;
          case TEMPLATE -> includeFileType = IncludeFileType.TEMPLATE;
          case COMPONENT -> includeFileType = IncludeFileType.COMPONENT;
          default -> includeFileType = IncludeFileType.LOCAL;
        }
        var include = new IncludeFile();
        include.setFileType(includeFileType);
        var path = handleQuotedText(keyValue.getValueText());
        include.setPath(path);
        ciAidYamlData.addInclude(include);
      });
    }
    var isChildOfProjectFileInclude = PsiUtils.isChild(scalar, List.of(FILE));
    if (isChildOfInclude && isChildOfProjectFileInclude && isNotChildOfRulesInputsIncludes) {
      // this is a project file include, cache the project file and skip rules/inputs
      var blockMappingOptional = PsiUtils.findParentOfType(scalar, YAMLBlockMappingImpl.class);
      if (blockMappingOptional.isPresent()) {
        var blockMapping = blockMappingOptional.get();
        if (!processedProjectIncludes.add(blockMapping)) {
          return;
        }
        var projectKeyValue = blockMapping.getKeyValueByKey(PROJECT);
        var fileKeyValue = blockMapping.getKeyValueByKey(FILE);
        if (projectKeyValue != null && fileKeyValue != null) {
          var projectPath = projectKeyValue.getValueText();
          var ref = blockMapping.getKeyValueByKey(REF);
          for (var filePath : getProjectIncludeFilePaths(fileKeyValue.getValue())) {
            var includeProject = new IncludeProject();
            includeProject.setFileType(IncludeFileType.PROJECT);
            includeProject.setProject(projectPath);
            if (ref != null) {
              includeProject.setRef(ref.getValueText());
            }
            includeProject.setPath(filePath);
            ciAidYamlData.addInclude(includeProject);
          }
        }
      }
    }
  }

  private List<String> getProjectIncludeFilePaths(YAMLValue value) {
    if (value instanceof YAMLScalar scalar) {
      return List.of(handleQuotedText(scalar.getText()));
    }
    if (value instanceof YAMLSequence sequence) {
      return sequence.getItems().stream()
              .map(YAMLSequenceItem::getValue)
              .filter(YAMLScalar.class::isInstance)
              .map(YAMLScalar.class::cast)
              .map(YAMLScalar::getText)
              .map(CIAidUtils::handleQuotedText)
              .toList();
    }
    return List.of();
  }

  private void readJobStage(@NotNull YAMLValue value) {
    var isChildOfStageElement = PsiUtils.isChild(value, List.of(STAGE));
    if (isChildOfStageElement) {
      ciAidYamlData.addJobStage(value);
    }
  }

  private void readInputs(@NotNull YAMLKeyValue keyValue) {
    boolean isSpecInputsElement = PsiUtils.findParent(keyValue, List.of(SPEC)).isPresent();
    if (isSpecInputsElement) {
      var value = keyValue.getValue();
      if (value instanceof YAMLBlockMappingImpl blockMapping) {
        blockMapping.getKeyValues()
                .forEach(ciAidYamlData::addInput);
      }
    }
  }

  private void readVariables(@NotNull YAMLKeyValue keyValue) {
    if (GitlabCIYamlUtils.isNotSpecInputsElement(keyValue)) {
      var value = keyValue.getValue();
      if (value instanceof YAMLBlockMappingImpl blockMapping) {
        blockMapping.getKeyValues()
                .forEach(ciAidYamlData::addVariable);
      }
    }
  }
}
