package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseReferenceProviderTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/ReferenceProviderTest");
  protected AbstractReferenceProvider referenceProvider;
  protected PsiElement psiYaml;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, "");
    var ciYamlFile = getGitlabCIYamlFile(rootDir);
    var project = getProject();
    project.getService(CIAidProjectService.class).readGitlabCIYamlData(ciYamlFile, false, false);
    psiYaml = getPsiGitlabCiYaml(rootDir);
  }

  protected List<YAMLPlainTextImpl> getBuildElements() {
    var buildStageElementsList = new ArrayList<YAMLPlainTextImpl>();
    findChildrenWithKey(psiYaml, "build", YAMLPlainTextImpl.class, buildStageElementsList);
    assertEquals(4, buildStageElementsList.size());
    return buildStageElementsList;
  }
}
