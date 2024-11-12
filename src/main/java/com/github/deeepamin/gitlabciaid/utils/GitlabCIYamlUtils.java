package com.github.deeepamin.gitlabciaid.utils;

import com.github.deeepamin.gitlabciaid.model.GitlabCIYamlData;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLBlockScalarImpl;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.INCLUDE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGES;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.TOP_LEVEL_KEYWORDS;

public class GitlabCIYamlUtils {
  // TODO Gitlab allows changing default file name, config for that?
  public static final String GITLAB_CI_DEFAULT_YAML_FILE = ".gitlab-ci.yml";
  private static final List<String> GITLAB_CI_YAML_FILES = new ArrayList<>();
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlUtils.class);

  static {
    GITLAB_CI_YAML_FILES.add(GITLAB_CI_DEFAULT_YAML_FILE);
  }

  public static void addYamlFile(final String yamlFilePath) {
    // used to add all the files included in .gitlab-ci.yml
    LOG.info("Found yaml file: " + yamlFilePath);
    GITLAB_CI_YAML_FILES.add(yamlFilePath);
  }

  public static boolean isGitlabCIYamlFile(final Path path) {
    return path != null && GITLAB_CI_YAML_FILES.stream().anyMatch(yamlFile -> path.toString().endsWith(yamlFile));
  }

  public static boolean isValidGitlabCIYamlFile(final VirtualFile file) {
    return file != null && file.isValid() && file.exists() && GITLAB_CI_YAML_FILES.stream().anyMatch(yamlFile -> file.getPath().endsWith(yamlFile));
  }

  public static Optional<Path> getGitlabCIYamlFile(final PsiElement psiElement) {
    return Optional.ofNullable(psiElement)
            .map(PsiElement::getContainingFile)
            .map(PsiFile::getOriginalFile)
            .map(PsiFile::getViewProvider)
            .map(FileViewProvider::getVirtualFile)
            .map(VirtualFile::getPath)
            .map(Path::of)
            .filter(GitlabCIYamlUtils::isGitlabCIYamlFile);
  }

  public static void parseGitlabCIYamlData(final Project project, final VirtualFile file, final GitlabCIYamlData gitlabCIYamlData) {
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

}
