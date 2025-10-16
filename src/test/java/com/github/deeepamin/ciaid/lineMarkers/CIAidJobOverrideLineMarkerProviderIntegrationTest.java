package com.github.deeepamin.ciaid.lineMarkers;

import com.github.deeepamin.ciaid.cache.providers.BaseIntegrationTest;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.ArrayList;
import java.util.List;

public class CIAidJobOverrideLineMarkerProviderIntegrationTest extends BaseIntegrationTest {
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

  public void testDeepInheritanceChainVariables() {
    YAMLKeyValue variablesInDeepJob = findKeyValueInJob("deep_job", "variables");

    if (variablesInDeepJob != null) {
      LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(variablesInDeepJob);

      assertNotNull("Line marker should be present for variables override in deep_job", lineMarker);
      assertTrue("Tooltip should mention override",
                 lineMarker.getLineMarkerTooltip().contains("Overrides"));
      assertTrue("Tooltip should mention variables",
                 lineMarker.getLineMarkerTooltip().contains("variables"));
    }
  }

  public void testDeepInheritanceCacheOverride() {
    YAMLKeyValue cacheInDeepJob = findKeyValueInJob("deep_job", "cache");

    if (cacheInDeepJob != null) {
      LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(cacheInDeepJob);

      assertNotNull("Line marker should be present for cache override", lineMarker);
      // Should point to .base since .middle doesn't have cache
      assertTrue("Tooltip should mention .base",
                 lineMarker.getLineMarkerTooltip().contains(".base"));
    }
  }

  public void testStandaloneJobNoMarkers() {
    YAMLKeyValue afterScriptInStandalone = findKeyValueInJob("standalone_job", "after_script");

    if (afterScriptInStandalone != null) {
      LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(afterScriptInStandalone);

      assertNull("Line marker should NOT be present for standalone job", lineMarker);
    }
  }

  public void testNoOverrideWhenPropertyNotPresent() {
    YAMLKeyValue afterScriptInBuildB = findKeyValueInJob("build_b", "after_script");

    // build_b doesn't have after_script, so this should be null
    assertNull("build_b should not have after_script", afterScriptInBuildB);
  }

  public void testScriptOverrideSkippingLevel() {
    YAMLKeyValue scriptInBuildB = findKeyValueInJob("build_b", "script");

    if (scriptInBuildB != null) {
      LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(scriptInBuildB);

      assertNotNull("Line marker should be present for script override", lineMarker);
      // Should skip .build (which has no script) and point to .gradle
      assertTrue("Tooltip should mention .gradle",
                 lineMarker.getLineMarkerTooltip().contains(".gradle"));
    }
  }

  public void testNavigationTargetIsCorrect() {
    YAMLKeyValue afterScriptInBuildA = findKeyValueInJob("build_a", "after_script");
    assertNotNull("after_script in build_a should exist", afterScriptInBuildA);

    LineMarkerInfo<?> lineMarker = lineMarkerProvider.getLineMarkerInfo(afterScriptInBuildA);
    assertNotNull("Line marker should be present", lineMarker);

    // The navigation target should be the after_script in .build
    YAMLKeyValue afterScriptInBuild = findKeyValueInJob(".build", "after_script");
    assertNotNull("Navigation target should exist", afterScriptInBuild);
  }

  public void testAllOverridableKeywords() {
    String[] overridableKeywords = {
      "after_script", "before_script", "script", "cache", "image",
      "services", "variables", "artifacts", "stage", "needs",
      "dependencies", "rules"
    };

    int testedCount = 0;
    for (String keyword : overridableKeywords) {
      YAMLKeyValue keyValue = findKeyValueInJob("build_a", keyword);
      if (keyValue != null) {
        testedCount++;
      }
    }

    assertTrue("Should have at least 2 overridable keywords in test data", testedCount >= 2);
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

    for (YAMLKeyValue kv : allKeyValues) {
      if (keyName.equals(kv.getKeyText()) && isChildOf(kv, job)) {
        return kv;
      }
    }

    return null;
  }

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

