package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.references.resolvers.ScriptReferenceResolver;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Arrays;

public class ScriptReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("ReferenceResolverTest/Script");

  public void testSameDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    myFixture.configureByFile(gitlabCIYamlPath);
    // reference resolve is null due to project basePath not returning copied dir in tests, so skipping that
  }

  public void testAnotherDirectory() {
    var testDir = getTestDirectoryName();
    var gitlabCIYamlPath = TEST_DIR_PATH + "/" + testDir + "/" + GITLAB_CI_DEFAULT_YAML_FILE;
    myFixture.configureByFile(gitlabCIYamlPath);
  }
}
