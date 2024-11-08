package com.github.deeepamin.gitlabciaid.utils;

import com.github.deeepamin.gitlabciaid.model.GitlabCIYamlData;
import com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords;
import com.github.deeepamin.gitlabciaid.model.yaml.Job;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLScalarList;
import org.jetbrains.yaml.psi.YAMLScalarText;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.AFTER_SCRIPT;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.BEFORE_SCRIPT;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.SCRIPT;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGE;
import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.STAGES;

public class GitlabCIYamlUtils {
  private static final List<String> SCHEMA_FILES = List.of(".gitlab-ci.yml");
  private static final Logger log = LoggerFactory.getLogger(GitlabCIYamlUtils.class);

  public static boolean isGitlabCIYamlFile(Path path) {
    return path != null && SCHEMA_FILES.stream().anyMatch(schemaFile -> path.getFileName().toString().endsWith(schemaFile));
  }

  public static boolean isValidGitlabCIYamlFile(VirtualFile file) {
    return file != null && file.isValid() && file.exists() && SCHEMA_FILES.stream().anyMatch(schemaFile -> file.getPath().endsWith(schemaFile));
  }

  public static Optional<Path> getGitlabCIYamlFile(PsiElement psiElement) {
    return Optional.ofNullable(psiElement)
            .map(PsiElement::getContainingFile)
            .map(PsiFile::getOriginalFile)
            .map(PsiFile::getViewProvider)
            .map(FileViewProvider::getVirtualFile)
            .map(VirtualFile::toNioPath)
            .filter(GitlabCIYamlUtils::isGitlabCIYamlFile);
  }

  public static GitlabCIYamlData parseGitlabCIYamlData(Project project, VirtualFile file) {
    var gitlabCIYamlData = new GitlabCIYamlData(file.getPath());

    ApplicationManager.getApplication().runReadAction(() -> {
      var psiManager = PsiManager.getInstance(project);
      var psiFile = psiManager.findFile(file);
      assert psiFile != null;
      psiFile.accept(new YamlRecursivePsiElementVisitor() {

        @Override
        public void visitDocument(@NotNull YAMLDocument document) {
          super.visitDocument(document);
        }

        @Override
        public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
          super.visitKeyValue(keyValue);
        }

        @Override
        public void visitMapping(@NotNull YAMLMapping mapping) {
          super.visitMapping(mapping);
        }

        @Override
        public void visitSequenceItem(@NotNull YAMLSequenceItem sequenceItem) {
          super.visitSequenceItem(sequenceItem);
        }

        @Override
        public void visitQuotedText(@NotNull YAMLQuotedText quotedText) {
          super.visitQuotedText(quotedText);
        }

        @Override
        public void visitValue(@NotNull YAMLValue value) {
          super.visitValue(value);
        }

        @Override
        public void visitSequence(@NotNull YAMLSequence sequence) {
          super.visitSequence(sequence);
        }

        @Override
        public void visitScalarList(@NotNull YAMLScalarList scalarList) {
          super.visitScalarList(scalarList);
        }

        @Override
        public void visitScalar(@NotNull YAMLScalar scalar) {
          super.visitScalar(scalar);
        }

        @Override
        public void visitScalarText(@NotNull YAMLScalarText scalarText) {
          super.visitScalarText(scalarText);
        }
      });

//      if (!(psiFile instanceof YAMLFile)) {
//        log.warn("PSI file is not a YAML file: {}", psiFile);
//        return;
//      }
//      var psiElements = psiFile.getChildren();
//      YAMLDocument yamlDocument = Arrays.stream(psiElements)
//                      .filter(YAMLDocument.class::isInstance)
//                      .findFirst()
//                      .map(YAMLDocument.class::cast)
//                      .orElse(null);
//      if (yamlDocument == null) {
//        log.warn("Could not find YAML document for {}", psiFile);
//        return;
//      }
//      YAMLMapping yamlMapping = Arrays.stream(yamlDocument.getChildren())
//              .filter(YAMLMapping.class::isInstance)
//              .findFirst()
//              .map(YAMLMapping.class::cast)
//              .orElse(null);
//      if (yamlMapping == null) {
//        log.warn("Could not find YAML mapping for {}", psiFile);
//        return;
//      }
//      // Root of yaml from here
//      readGitlabCIYamlData(yamlMapping, gitlabCIYamlData, true);
      System.out.println(psiFile);

    });
    return gitlabCIYamlData;
  }

  private static void readGitlabCIYamlData(PsiElement psiElement, GitlabCIYamlData gitlabCIYamlData, boolean isTopLevel) {
    if (psiElement == null) {
      return;
    }
    for(PsiElement child : psiElement.getChildren()) {
      if (child instanceof YAMLKeyValue yamlKeyValue) {
        var keyElement = yamlKeyValue.getKey();
        var keyText = yamlKeyValue.getKeyText();
        var valueElement = yamlKeyValue.getValue();
//        var valueText = yamlKeyValue.getValueText();

        if (isTopLevel && !GitlabCIYamlKeywords.TOP_LEVEL_KEYWORDS.contains(keyText)) {
          // this is a job
          Job job = new Job();
          job.setName(keyText);
          readJob(valueElement, job);
          return;
        }

        if (keyText.equals(STAGES)) {

        }
      } else {
        System.out.println("child instance of " + child.getClass()+ " " + child.getText());
      }
      readGitlabCIYamlData(child, gitlabCIYamlData, false);
    }
  }

  private static void readJob(PsiElement jobValueElement, Job job) {
    if (jobValueElement == null) {
      return;
    }
    for(PsiElement child : jobValueElement.getChildren()) {
      if (child instanceof YAMLKeyValue yamlKeyValue) {
//        var keyElement = yamlKeyValue.getKey();
        var keyText = yamlKeyValue.getKeyText();
        var valueElement = yamlKeyValue.getValue();

        switch (keyText) {
          case STAGE -> job.setStageName(yamlKeyValue.getValueText());
          case SCRIPT -> {
            for (var scriptChild: valueElement.getChildren()) {
              if (valueElement instanceof YAMLSequence yamlSequence) {
                job.addScript(yamlSequence.getText());
              }
            }
          }
        }
      }
      readJob(child, job);
    }

  }
}
