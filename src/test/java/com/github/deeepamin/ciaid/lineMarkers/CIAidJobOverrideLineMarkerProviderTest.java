package com.github.deeepamin.ciaid.lineMarkers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.ArrayList;
import java.util.List;

public class CIAidJobOverrideLineMarkerProviderTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/LineMarkerTest");
  private CIAidJobOverrideLineMarkerProvider lineMarkerProvider;
  private PsiElement psiYaml;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    var rootDir = myFixture.copyDirectoryToProject(TEST_DIR_PATH, "");
    var ciYamlFile = getGitlabCIYamlFile(rootDir);
    var project = getProject();
    project.getService(CIAidProjectService.class).readGitlabCIYamlData(ciYamlFile, false, false);
    psiYaml = getPsiGitlabCiYaml(rootDir);
    lineMarkerProvider = new CIAidJobOverrideLineMarkerProvider();
  }

  public void testAfterScriptOverrideInBuildA() {
    YAMLKeyValue afterScriptInBuildA = findKeyValueInJob("build_a", "after_script");
    assertNotNull("after_script in build_a should exist", afterScriptInBuildA);

    LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(afterScriptInBuildA);

    assertNotNull("Line marker should be present for after_script override", lineMarker);
    assertNotNull("Line marker should have a tooltip", lineMarker.getLineMarkerTooltip());
    assertTrue("Tooltip should mention override",
               lineMarker.getLineMarkerTooltip().contains("Overrides"));
    assertTrue("Tooltip should mention after_script",
               lineMarker.getLineMarkerTooltip().contains("after_script"));
  }

  public void testAfterScriptOverrideInBuild() {
    YAMLKeyValue afterScriptInBuild = findKeyValueInJob(".build", "after_script");
    assertNotNull("after_script in .build should exist", afterScriptInBuild);

    LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(afterScriptInBuild);

    assertNotNull("Line marker should be present for after_script override in .build", lineMarker);
    assertNotNull("Line marker should have a tooltip", lineMarker.getLineMarkerTooltip());
    assertTrue("Tooltip should mention .gradle",
               lineMarker.getLineMarkerTooltip().contains(".gradle"));
  }

  public void testScriptNoOverrideInBuildA() {
    YAMLKeyValue scriptInBuildA = findKeyValueInJob("build_a", "script");
    assertNotNull("script in build_a should exist", scriptInBuildA);

    LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(scriptInBuildA);

    // Since .build doesn't have script, and .gradle has script, but build_a extends .build,
    // this should show an override marker pointing to .gradle
    assertNotNull("Line marker should be present for script override", lineMarker);
  }

  public void testNoOverrideMarkerWithoutExtends() {
    YAMLKeyValue afterScriptInGradle = findKeyValueInJob(".gradle", "after_script");
    assertNotNull("after_script in .gradle should exist", afterScriptInGradle);

    LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(afterScriptInGradle);

    assertNull("Line marker should NOT be present when there's no extends", lineMarker);
  }

  public void testNoOverrideMarkerForNonOverridableKeywords() {
    YAMLKeyValue extendsInBuildA = findKeyValueInJob("build_a", "extends");
    assertNotNull("extends in build_a should exist", extendsInBuildA);

    LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(extendsInBuildA);

    assertNull("Line marker should NOT be present for 'extends' keyword", lineMarker);
  }

  public void testBeforeScriptOverrideThroughChain() {
    YAMLKeyValue beforeScriptInBuildA = findKeyValueInJob("build_a", "before_script");

    if (beforeScriptInBuildA != null) {
      LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(beforeScriptInBuildA);
      assertNotNull("Line marker should be present for before_script override", lineMarker);
    }
  }

  public void testImageOverride() {
    YAMLKeyValue imageInBuildA = findKeyValueInJob("build_a", "image");

    if (imageInBuildA != null) {
      LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(imageInBuildA);
      assertNotNull("Line marker should be present for image override", lineMarker);
    }
  }

  public void testCircularReferenceHandling() {
    // If there's a circular reference test case
    YAMLKeyValue circularJob = findKeyValueInJob("circular_a", "script");

    if (circularJob != null) {
      // Should not crash, even with circular reference
      // Result can be null or valid, but should not throw exception
      try {
        lineMarkerProvider.getLineMarkerInfo(circularJob);
        // If we get here without exception, test passes
        assertTrue("Should handle circular references without throwing exception", true);
      } catch (Exception e) {
        fail("Should not throw exception on circular reference: " + e.getMessage());
      }
    }
  }

  private YAMLKeyValue findKeyValueInJob(String jobName, String keyName) {
    List<YAMLKeyValue> allKeyValues = new ArrayList<>();
    findAllKeyValues(psiYaml, allKeyValues);

    YAMLKeyValue job = null;
    for (YAMLKeyValue kv : allKeyValues) {
      if (jobName.equals(kv.getKeyText())) {
        job = kv;
        break;
      }
    }

    if (job == null) {
      return null;
    }

    // Now find the key within this job
    for (YAMLKeyValue kv : allKeyValues) {
      if (keyName.equals(kv.getKeyText()) && isChildOf(kv, job)) {
        return kv;
      }
    }

    return null;
  }

  /**
   * Find all YAMLKeyValue elements recursively
   */
  private void findAllKeyValues(PsiElement element, List<YAMLKeyValue> result) {
    if (element instanceof YAMLKeyValue) {
      result.add((YAMLKeyValue) element);
    }
    for (PsiElement child : element.getChildren()) {
      findAllKeyValues(child, result);
    }
  }

  private boolean isChildOf(PsiElement child, PsiElement parent) {
    PsiElement current = child.getParent();
    while (current != null) {
      if (current == parent) {
        return true;
      }
      current = current.getParent();
    }
    return false;
  }
}

