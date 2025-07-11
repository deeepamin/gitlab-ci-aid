package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.StagesReferenceResolver;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

public class StagesReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetReferencesStagesToJobStage() {
    var buildStageElementsList = getBuildElements();
    var stageElementInStagesBlock = buildStageElementsList.stream()
            .filter(stage -> stage.getParent() instanceof YAMLSequenceItem)
            .findFirst()
            .orElse(null);
    assertNotNull(stageElementInStagesBlock);
    var stageElementsOnJobLevel = buildStageElementsList.stream()
            .filter(stage -> stage.getParent() instanceof YAMLKeyValue)
            .toList();
    assertNotNull(stageElementsOnJobLevel);
    assertEquals(3, stageElementsOnJobLevel.size());
    referenceProvider = new StagesReferenceProvider(stageElementInStagesBlock);
    var stagesToStageReference = referenceProvider.getElementReferences();
    assertNotNull(stagesToStageReference);
    assertTrue(stagesToStageReference.isPresent());
    assertEquals(1, stagesToStageReference.get().length);
    var stagesToStageReferenceResolver = stagesToStageReference.get()[0];
    assertTrue(stagesToStageReferenceResolver instanceof StagesReferenceResolver);
    assertEquals(stageElementInStagesBlock, stagesToStageReferenceResolver.getElement());
    var stageOnJobTargets = ((StagesReferenceResolver) stagesToStageReferenceResolver).getTargets();
    assertTrue(stageOnJobTargets.containsAll(stageElementsOnJobLevel));
  }

}
