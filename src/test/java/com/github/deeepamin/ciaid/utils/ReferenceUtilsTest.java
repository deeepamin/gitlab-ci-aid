package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.services.GitlabCIYamlProjectService;
import com.github.deeepamin.ciaid.services.resolvers.IncludeFileReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.JobStageToStagesReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.NeedsOrExtendsToJobReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.ScriptReferenceResolver;
import com.github.deeepamin.ciaid.services.resolvers.StagesToJobStageReferenceResolver;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.ArrayList;
import java.util.List;

public class ReferenceUtilsTest extends BaseTest {
  private static final String TEST_DIR_PATH = "/UtilsTest";
  private PsiElement psiYaml;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, "");
    var ciYamlFile = getGitlabCIYamlFile(rootDir);
    var project = getProject();
    project.getService(GitlabCIYamlProjectService.class).readGitlabCIYamlData(project, ciYamlFile);
    psiYaml = getPsiGitlabCiYaml(rootDir);
  }

  public void testGetReferencesScript() {
    var deployScriptElement = findChildWithKey(psiYaml, "./deploy.sh");
    var reference = ReferenceUtils.getReferences(deployScriptElement);
    assertNotNull(reference);
    assertTrue(reference.isPresent());
    assertEquals(1, reference.get().length);
    assertTrue(reference.get()[0] instanceof ScriptReferenceResolver);
    assertEquals(deployScriptElement, reference.get()[0].getElement());
  }

  public void testGetReferencesIncludeFile() {
    var includeFileElement = findChildWithKey(psiYaml, "/pipeline.yml");
    var referenceIncludeFile = ReferenceUtils.getReferences(includeFileElement);
    assertNotNull(referenceIncludeFile);
    assertTrue(referenceIncludeFile.isPresent());
    assertEquals(1, referenceIncludeFile.get().length);
    assertTrue(referenceIncludeFile.get()[0] instanceof IncludeFileReferenceResolver);
    assertEquals(includeFileElement, referenceIncludeFile.get()[0].getElement());
  }

  public void testGetReferencesNeedsToJob() {
    var needsJobElement = findChildWithKey(psiYaml, "\"test-job\"");
    assertNotNull(needsJobElement);
    var referenceNeeds = ReferenceUtils.getReferences(needsJobElement);
    assertNotNull(referenceNeeds);
    assertTrue(referenceNeeds.isPresent());
    assertEquals(1, referenceNeeds.get().length);
    assertTrue(referenceNeeds.get()[0] instanceof NeedsOrExtendsToJobReferenceResolver);
    assertEquals(needsJobElement, referenceNeeds.get()[0].getElement());
  }

  public void testGetReferencesExtendsToJob() {
    var extendsJobElement = findChildWithKey(psiYaml, "\"test-job\"");
    assertNotNull(extendsJobElement);
    var referenceExtends = ReferenceUtils.getReferences(extendsJobElement);
    assertNotNull(referenceExtends);
    assertTrue(referenceExtends.isPresent());
    assertEquals(1, referenceExtends.get().length);
    assertTrue(referenceExtends.get()[0] instanceof NeedsOrExtendsToJobReferenceResolver);
    assertEquals(extendsJobElement, referenceExtends.get()[0].getElement());
  }

  public void testGetReferencesStagesToStage() {
    var buildStageElementsList = getBuildElements();
    var stageElementInStagesBlock = buildStageElementsList.stream()
            .filter(stage -> stage.getParent() instanceof YAMLSequenceItem)
            .findFirst()
            .orElse(null);
    assertNotNull(stageElementInStagesBlock);
    var stageElementsOnJobLevel = buildStageElementsList.stream()
            .filter(stage -> stage.getParent() instanceof YAMLKeyValue)
            .map(stage -> (YAMLKeyValue) stage.getParent())
            .toList();
    assertNotNull(stageElementsOnJobLevel);
    assertEquals(3, stageElementsOnJobLevel.size());
    var stagesToStageReference = ReferenceUtils.getReferences(stageElementInStagesBlock);
    assertNotNull(stagesToStageReference);
    assertTrue(stagesToStageReference.isPresent());
    assertEquals(1, stagesToStageReference.get().length);
    var stagesToStageReferenceResolver = stagesToStageReference.get()[0];
    assertTrue(stagesToStageReferenceResolver instanceof StagesToJobStageReferenceResolver);
    assertEquals(stageElementInStagesBlock, stagesToStageReferenceResolver.getElement());
    var stageOnJobTargets = ((StagesToJobStageReferenceResolver) stagesToStageReferenceResolver).getTargets();
    assertTrue(stageOnJobTargets.containsAll(stageElementsOnJobLevel));
  }

  public void testGetReferencesStagesToStages() {
    var buildStageElementsList = getBuildElements();
    var stageElementInStagesBlock = buildStageElementsList.stream()
            .filter(stage -> stage.getParent() instanceof YAMLSequenceItem)
            .findFirst()
            .orElse(null);
    assertNotNull(stageElementInStagesBlock);
    var stageElementOnJobLevel = buildStageElementsList.stream()
            .filter(stage -> stage.getParent() instanceof YAMLKeyValue)
            .findFirst()
            .orElse(null);
    assertNotNull(stageElementOnJobLevel);
    var stageToStagesReference = ReferenceUtils.getReferences(stageElementOnJobLevel);
    assertNotNull(stageToStagesReference);
    assertTrue(stageToStagesReference.isPresent());
    assertEquals(1, stageToStagesReference.get().length);
    assertTrue(stageToStagesReference.get()[0] instanceof JobStageToStagesReferenceResolver);
    assertEquals(stageElementOnJobLevel, stageToStagesReference.get()[0].getElement());
  }

  public void testHandleQuotedText() {
    assertEquals("test", ReferenceUtils.handleQuotedText("\"test\""));
    assertEquals("\"test", ReferenceUtils.handleQuotedText("\"test"));
    assertEquals("test\"", ReferenceUtils.handleQuotedText("test\""));
    assertEquals("test", ReferenceUtils.handleQuotedText("'test'"));
    assertEquals("'test", ReferenceUtils.handleQuotedText("'test"));
    assertEquals("test'", ReferenceUtils.handleQuotedText("test'"));
  }

  private List<YAMLPlainTextImpl> getBuildElements() {
    var buildStageElementsList = new ArrayList<YAMLPlainTextImpl>();
    findChildrenWithKey(psiYaml, "build", YAMLPlainTextImpl.class, buildStageElementsList);
    assertEquals(4, buildStageElementsList.size());
    return buildStageElementsList;
  }
}
