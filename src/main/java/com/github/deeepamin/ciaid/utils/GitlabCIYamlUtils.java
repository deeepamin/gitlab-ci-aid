package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.services.GitlabCIYamlProjectService;
import com.github.deeepamin.ciaid.services.providers.EditorNotificationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GitlabCIYamlUtils {
  // TODO Gitlab allows changing default file name, config for that?
  public static final String GITLAB_CI_DEFAULT_YML_FILE = ".gitlab-ci.yml";
  public static final String GITLAB_CI_DEFAULT_YAML_FILE = ".gitlab-ci.yaml";
  public static final List<String> GITLAB_CI_DEFAULT_YAML_FILES = List.of(GITLAB_CI_DEFAULT_YML_FILE, GITLAB_CI_DEFAULT_YAML_FILE);

  private static final Set<String> GITLAB_CI_YAML_FILES = new HashSet<>(GITLAB_CI_DEFAULT_YAML_FILES);
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlUtils.class);

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

  public static boolean isGitlabYamlFile(final VirtualFile file) {
    return file != null && (isValidGitlabCIYamlFile(file) || EditorNotificationProvider.isMarkedAsGitLabYamlFile(file));
  }

  public static boolean hasGitlabYamlFile(final PsiElement psiElement) {
    return getVirtualFile(psiElement).filter(GitlabCIYamlUtils::isGitlabYamlFile).isPresent();
  }

  public static Optional<Path> getGitlabCIYamlFile(final PsiElement psiElement) {
    try {
      return getVirtualFile(psiElement)
              .map(VirtualFile::getPath)
              .map(Path::of)
              .filter(GitlabCIYamlUtils::isGitlabCIYamlFile);
    } catch (InvalidPathException ipX) {
      return Optional.empty();
    }
  }

  private static @NotNull Optional<VirtualFile> getVirtualFile(PsiElement psiElement) {
    return Optional.ofNullable(psiElement)
            .map(PsiElement::getContainingFile)
            .map(PsiFile::getOriginalFile)
            .map(PsiFile::getViewProvider)
            .map(FileViewProvider::getVirtualFile);
  }

  public static GitlabCIYamlProjectService getGitlabCIYamlProjectService(PsiElement psiElement) {
    var service = GitlabCIYamlProjectService.getInstance(psiElement.getProject());
    if (service == null) {
      throw new IllegalStateException("Cannot find gitlab CI yaml project service: " + psiElement.getProject().getName());
    }
    return service;
  }

  public static boolean isYamlFile(VirtualFile file) {
    return file != null && file.isValid() && !file.isDirectory() && (file.getPath().endsWith(".yml") || file.getPath().endsWith(".yaml"));
  }
}
