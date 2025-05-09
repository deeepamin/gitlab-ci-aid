package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.BaseTest;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.List;

public class PsiUtilsTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/UtilsTest");
  private VirtualFile rootDir;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, TEST_DIR_PATH);
  }

  public void testIsChild() {
    var psiYaml = getPsiGitlabCiYaml(rootDir);
    var buildDevTag = findChildWithKey(psiYaml, "build-dev-tag");
    assertNotNull(buildDevTag);
    assertTrue(PsiUtils.isChild(buildDevTag, List.of("build-dev")));
    var buildSitTag = findChildWithKey(psiYaml, "build-sit-tag");
    assertNotNull(buildSitTag);
    assertTrue(PsiUtils.isChild(buildSitTag, List.of("build-sit")));

    assertFalse(PsiUtils.isChild(buildDevTag, List.of("build-sit")));
    assertFalse(PsiUtils.isChild(buildSitTag, List.of("build-dev")));
  }

  public void testFindChildren() {
    var psiYaml = getPsiGitlabCiYaml(rootDir);
    var quotedTextChildren = PsiUtils.findChildren(psiYaml, YAMLQuotedText.class);
    assertEquals(4, quotedTextChildren.size());
    var plainTextChildren = PsiUtils.findChildren(psiYaml, YAMLPlainTextImpl.class);
    assertEquals(33, plainTextChildren.size());

    var pipelinePsiYml = getPsiCiPipelineYaml(rootDir);
    var quotedTextChildrenCI = PsiUtils.findChildren(pipelinePsiYml, YAMLQuotedText.class);
    assertEquals(1, quotedTextChildrenCI.size());
    var plainTextChildrenCI = PsiUtils.findChildren(pipelinePsiYml, YAMLPlainTextImpl.class);
    assertEquals(23, plainTextChildrenCI.size());
  }

  public void testIsYamlTextElement() {
    var psiYaml = getPsiGitlabCiYaml(rootDir);
    var quotedTextChild = PsiUtils.findChildren(psiYaml, YAMLQuotedText.class).getFirst();
    assertTrue(YamlUtils.isYamlTextElement(quotedTextChild));
    var plainTextChild = findFirstChild(psiYaml, YAMLPlainTextImpl.class);
    assertTrue(YamlUtils.isYamlTextElement(plainTextChild));
    var keyValueChild = findFirstChild(psiYaml, YAMLKeyValue.class);
    assertFalse(YamlUtils.isYamlTextElement(keyValueChild));

    var pipelinePsiYml = getPsiCiPipelineYaml(rootDir);
    var quotedTextChildCI = PsiUtils.findChildren(pipelinePsiYml, YAMLQuotedText.class).getFirst();
    assertTrue(YamlUtils.isYamlTextElement(quotedTextChildCI));
    var plainTextChildCI = findFirstChild(pipelinePsiYml, YAMLPlainTextImpl.class);
    assertTrue(YamlUtils.isYamlTextElement(plainTextChildCI));
    var keyValueChildCI = findFirstChild(pipelinePsiYml, YAMLKeyValue.class);
    assertFalse(YamlUtils.isYamlTextElement(keyValueChildCI));
  }

  public void testHasChild() {
    var psiYaml = getPsiGitlabCiYaml(rootDir);
    assertTrue(PsiUtils.hasChild(psiYaml, "image"));
    assertTrue(PsiUtils.hasChild(psiYaml, "services"));
    assertTrue(PsiUtils.hasChild(psiYaml, "build-dev"));
    assertTrue(PsiUtils.hasChild(psiYaml, "deploy-job"));
    assertFalse(PsiUtils.hasChild(psiYaml, "deploy-dev"));
  }
}
