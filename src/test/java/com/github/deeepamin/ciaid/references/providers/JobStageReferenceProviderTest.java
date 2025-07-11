package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.JobStageReferenceResolver;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

public class JobStageReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetReferencesJobStageToStages() {
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
    referenceProvider = new JobStageReferenceProvider(stageElementOnJobLevel);
    var stageToStagesReference = referenceProvider.getElementReferences();
    assertNotNull(stageToStagesReference);
    assertTrue(stageToStagesReference.isPresent());
    assertEquals(1, stageToStagesReference.get().length);
    assertTrue(stageToStagesReference.get()[0] instanceof JobStageReferenceResolver);
    assertEquals(stageElementOnJobLevel, stageToStagesReference.get()[0].getElement());
  }
}
