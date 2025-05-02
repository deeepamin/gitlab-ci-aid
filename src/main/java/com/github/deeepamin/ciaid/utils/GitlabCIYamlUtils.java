package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.services.GitlabCIYamlProjectService;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Optional;

public class GitlabCIYamlUtils {
  // TODO Gitlab allows changing default file name, config for that?
  public static final String GITLAB_CI_DEFAULT_YML_FILE = ".gitlab-ci.yml";
  public static final String GITLAB_CI_DEFAULT_YAML_FILE = ".gitlab-ci.yaml";
  public static final List<String> GITLAB_CI_DEFAULT_YAML_FILES = List.of(GITLAB_CI_DEFAULT_YML_FILE, GITLAB_CI_DEFAULT_YAML_FILE);

  public static final Key<Boolean> GITLAB_CI_YAML_MARKED_KEY = Key.create("CIAid.Gitlab.YAML");
  public static final Key<Boolean> GITLAB_CI_YAML_USER_MARKED_KEY = Key.create("CIAid.Gitlab.User.YAML");

  public static boolean isValidGitlabCIYamlFile(final VirtualFile file) {
    return file != null && file.isValid() && file.exists()
            && (GITLAB_CI_DEFAULT_YAML_FILES
                      .stream()
                      .anyMatch(yamlFile -> file.getPath().endsWith(yamlFile)
            || isMarkedAsCIYamlFile(file)
            || isMarkedAsUserCIYamlFile(file)));
  }

  public static boolean hasGitlabYamlFile(final PsiElement psiElement) {
    return getGitlabCIYamlFile(psiElement).isPresent();
  }

  public static Optional<VirtualFile> getGitlabCIYamlFile(final PsiElement psiElement) {
    try {
      return Optional.ofNullable(psiElement)
              .map(PsiElement::getContainingFile)
              .map(PsiFile::getOriginalFile)
              .map(PsiFile::getViewProvider)
              .map(FileViewProvider::getVirtualFile)
              .filter(GitlabCIYamlUtils::isValidGitlabCIYamlFile);
    } catch (InvalidPathException ipX) {
      return Optional.empty();
    }
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

  public static void markAsUserCIYamlFile(VirtualFile file) {
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, true);
  }

  public static boolean isMarkedAsUserCIYamlFile(VirtualFile file) {
    return Boolean.TRUE.equals(file.getUserData(GITLAB_CI_YAML_USER_MARKED_KEY));
  }

  public static void markAsCIYamlFile(VirtualFile file) {
    file.putUserData(GITLAB_CI_YAML_MARKED_KEY, true);
  }

  public static boolean isMarkedAsCIYamlFile(VirtualFile file) {
    return Boolean.TRUE.equals(file.getUserData(GITLAB_CI_YAML_MARKED_KEY));
  }
}
