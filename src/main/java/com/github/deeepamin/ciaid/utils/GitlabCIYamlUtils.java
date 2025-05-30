package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.FileUtils.StringWithStartEndRange;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.impl.YAMLArrayImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.github.deeepamin.ciaid.utils.ReferenceUtils.handleQuotedText;

public class GitlabCIYamlUtils {
  public static final String GITLAB_CI_DEFAULT_YML_FILE = ".gitlab-ci.yml";
  public static final String GITLAB_CI_DEFAULT_YAML_FILE = ".gitlab-ci.yaml";
  public static final List<String> GITLAB_CI_DEFAULT_YAML_FILES = List.of(GITLAB_CI_DEFAULT_YML_FILE, GITLAB_CI_DEFAULT_YAML_FILE);

  public static final Key<Boolean> GITLAB_CI_YAML_MARKED_KEY = Key.create("CIAid.Gitlab.YAML");
  public static final Key<Boolean> GITLAB_CI_YAML_USER_MARKED_KEY = Key.create("CIAid.Gitlab.User.YAML");

  public static final String REFERENCE_TAG = "!reference";

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
    return Optional.ofNullable(psiElement)
            .map(PsiElement::getContainingFile)
            .map(PsiFile::getOriginalFile)
            .map(PsiFile::getViewProvider)
            .map(FileViewProvider::getVirtualFile)
            .filter(GitlabCIYamlUtils::isValidGitlabCIYamlFile);
  }

  public static CIAidProjectService getGitlabCIYamlProjectService(PsiElement psiElement) {
    var service = CIAidProjectService.getInstance(psiElement.getProject());
    if (service == null) {
      throw new IllegalStateException("Cannot find gitlab CI yaml project service: " + psiElement.getProject().getName());
    }
    return service;
  }

  public static void markAsUserCIYamlFile(VirtualFile file, Project project) {
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, true);
    CIAidSettingsState.getInstance(project).addYamlToUserMarking(file, false);
  }

  public static void ignoreCIYamlFile(VirtualFile file, Project project) {
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, false);
    CIAidSettingsState.getInstance(project).addYamlToUserMarking(file, true);
  }

  public static void removeMarkingOfUserCIYamlFile(VirtualFile file) {
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, null);
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

  public static boolean isAnInputsString(String input) {
    if (input == null) {
      return false;
    }
    var inputsWithStartEndRange = getInputs(input);
    return inputsWithStartEndRange != null && !inputsWithStartEndRange.isEmpty();
  }

  public static List<StringWithStartEndRange> getInputs(String input) {
    if (input == null) {
      return null;
    }
    var pattern = Pattern.compile("\\$\\[\\[\\s*(inputs\\.\\w+)\\s*]]");
    var matcher = pattern.matcher(input);
    var inputs = new ArrayList<StringWithStartEndRange>();
    while (matcher.find()) {
      inputs.add(new StringWithStartEndRange(matcher.group(1), matcher.start(1), matcher.end(1)));
    }
    return inputs;
  }

  public static List<StringWithStartEndRange> getInputNames(String input) {
    if (input == null) {
      return null;
    }
    var pattern = Pattern.compile("\\$\\[\\[\\s*inputs\\.(\\w+)");
    var matcher = pattern.matcher(input);
    var inputs = new ArrayList<StringWithStartEndRange>();
    while (matcher.find()) {
      inputs.add(new StringWithStartEndRange(matcher.group(1), matcher.start(1), matcher.end(1)));
    }
    return inputs;
  }

  public static String getReferenceTag(YAMLPsiElement element) {
    var parent = PsiUtils.findParentOfType(element, YAMLArrayImpl.class);
    if (parent.isPresent()) {
      var firstChild = parent.get().getFirstChild();
      if (firstChild != null && firstChild.getText().equals(REFERENCE_TAG)) {
        var children = parent.get().getChildren();
        if (children.length > 0) {
          var refersToText = handleQuotedText(children[0].getText());
          if (element.getText() != null && element.getText().equals(refersToText)) {
            return refersToText;
          }
          if (children.length > 1) {
            var keyToReferToText = handleQuotedText(children[1].getText());
            if (element.getText() != null && element.getText().equals(keyToReferToText)) {
              // still return refersToText, as it is the reference tag and the key is resolved in resolver
              return refersToText;
            }
          }
        }
      }
    }
    return null;
  }

  public static List<StringWithStartEndRange> getVariables(String text) {
    if (text == null) {
      return null;
    }
    var vars = new ArrayList<StringWithStartEndRange>();
    var pattern = Pattern.compile("\\$\\{?(\\w+)}?");
    var matcher = pattern.matcher(text);
    while (matcher.find()) {
      vars.add(new StringWithStartEndRange(matcher.group(1), matcher.start(1), matcher.end(1)));
    }
    return vars;
  }
}
