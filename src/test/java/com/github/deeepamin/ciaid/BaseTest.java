package com.github.deeepamin.ciaid;

import com.github.deeepamin.ciaid.services.GitlabCIYamlProjectService;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class BaseTest extends BasePlatformTestCase {
  protected static final String GITLAB_CI_DEFAULT_YAML_FILE = ".gitlab-ci.yml";
  protected static final String PIPELINE_YML = "/pipeline.yml";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    GitlabCIYamlUtils.addYamlFile(PIPELINE_YML.replace("/", File.separator));
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/resources/testdata";
  }

  protected VirtualFile getGitlabCIYamlFile(VirtualFile rootDir) {
    return Arrays.stream(rootDir.getChildren())
            .filter(file -> file.getPath().contains(GITLAB_CI_DEFAULT_YAML_FILE))
            .findFirst()
            .orElse(null);
  }

  protected VirtualFile getCIPipelineYamlFile(VirtualFile rootDir) {
    return Arrays.stream(rootDir.getChildren())
            .filter(file -> file.getPath().contains(PIPELINE_YML))
            .findFirst()
            .orElse(null);
  }

  protected VirtualFile getYamlFile(VirtualFile rootDir, String fileName) {
    return Arrays.stream(rootDir.getChildren())
            .filter(file -> file.getPath().contains(fileName))
            .findFirst()
            .orElse(null);
  }


  protected PsiElement getPsiGitlabCiYaml(VirtualFile rootDir) {
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    var psiYaml = getPsiManager().findFile(gitlabCIYaml);
    assertNotNull(psiYaml);
    return psiYaml;
  }

  protected PsiElement getPsiCiPipelineYaml(VirtualFile rootDir) {
    var ciPipelineYaml = getCIPipelineYamlFile(rootDir);
    var psiYaml = getPsiManager().findFile(ciPipelineYaml);
    assertNotNull(psiYaml);
    return psiYaml;
  }

  public static <T extends PsiElement> PsiElement findFirstChild(final PsiElement element, final Class<T> clazz) {
    if (element == null) {
      return null;
    }
    for (PsiElement child : element.getChildren()) {
      if (clazz.isInstance(child)) {
        return child;
      }
      var found = findFirstChild(child, clazz);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  public static PsiElement findChildWithKey(final PsiElement element, final String childKey) {
    if (element == null) {
      return null;
    }
    for (PsiElement child : element.getChildren()) {
      if ((child instanceof YAMLPlainTextImpl || child instanceof YAMLQuotedText) && childKey.equals(child.getText())) {
        return child;
      }
      var found = findChildWithKey(child, childKey);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static  <T extends PsiElement> void findChildrenWithKey(final PsiElement element, final String childKey, final Class<T> clazz, final List<T> children) {
    if (element == null) {
      return;
    }
    if (clazz.isInstance(element) && childKey.equals(element.getText())) {
      children.add((T) element);
    }
    for (PsiElement child : element.getChildren()) {
      findChildrenWithKey(child, childKey, clazz, children);
    }
  }

  public void readCIYamls(VirtualFile rootDir) {
    var project = getProject();
    var projectService = GitlabCIYamlProjectService.getInstance(project);
    var ciYamlFile = getGitlabCIYamlFile(rootDir);
    projectService.readGitlabCIYamlData(project, ciYamlFile);
    // included file should get read with read code, but the base path in test isn't allowing that even after copying at beginning of test
    var pipelineYamlFile = getCIPipelineYamlFile(rootDir);
    if (pipelineYamlFile != null) {
      projectService.readGitlabCIYamlData(project, pipelineYamlFile);
    }
  }

}
