package com.github.deeepamin.ciaid.parser;

import com.github.deeepamin.ciaid.services.CIAidYamlDataProviderTest;
import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.List;
import java.util.function.Function;

public class CIAidGitLabYamlParserTest extends CIAidYamlDataProviderTest {
  public void testParseGitlabCIYamlDataValidFiles() {
    var gitlabCIYaml = getGitlabCIYamlFile(rootDir);
    var parser = new CIAidGitLabYamlParser(getProject());
    var gitlabCIYamlData = parser.parseGitlabCIYamlData(gitlabCIYaml);

    var includedYamls = gitlabCIYamlData.getIncludes();
    assertEquals(1, includedYamls.size());
    assertEquals(PIPELINE_YML_PATH, includedYamls.getFirst().getPath());
    var expectedStages = List.of("build", "test", "deploy");
    var stageNames = gitlabCIYamlData.getJobStageElements().stream()
            .map(PsiElement::getText)
            .distinct()
            .toList();
    assertEquals(3, stageNames.size());
    assertTrue(expectedStages.containsAll(stageNames));
    Function<String, List<String>> jobStageFunction = (String stageName) -> gitlabCIYamlData.getJobStageElements().stream()
            .map(PsiElement::getText)
            .filter(stage -> stage.equals(stageName))
            .toList();
    assertEquals(3, jobStageFunction.apply("build").size());
    assertEquals(1, jobStageFunction.apply("test").size());
    assertEquals(1, jobStageFunction.apply("deploy").size());

    var expectedJobNames = List.of(".extend-test","build-dev", "build-sit", "test-job", "deploy-job");
    var jobNames = gitlabCIYamlData.getJobElements().stream()
            .map(YAMLKeyValue::getKeyText)
            .toList();
    assertEquals(5, jobNames.size());
    assertTrue(expectedJobNames.containsAll(jobNames));
    assertNotNull(gitlabCIYamlData.getFile());
    assertNotNull(gitlabCIYamlData.getStagesItemElements());
  }
}
