package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.StagesReferenceResolver;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

public class StagesReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetReferencesStagesToJob() {
    var buildStageElementsList = getBuildElements();
    var stageElementInStagesBlock = buildStageElementsList.stream()
            .filter(stage -> stage.getParent() instanceof YAMLSequenceItem)
            .findFirst()
            .orElse(null);
    assertNotNull(stageElementInStagesBlock);
    var jobElements = buildStageElementsList.stream()
            .filter(stage -> stage.getParent() instanceof YAMLKeyValue)
            .map(stage -> (YAMLKeyValue) stage.getParent().getParent().getParent())
            .toList();
    assertNotNull(jobElements);
    assertEquals(3, jobElements.size());
    referenceProvider = new StagesReferenceProvider(stageElementInStagesBlock);
    var stagesToStageReference = referenceProvider.getElementReferences();
    assertNotNull(stagesToStageReference);
    assertTrue(stagesToStageReference.isPresent());
    assertEquals(1, stagesToStageReference.get().length);
    var stagesToStageReferenceResolver = stagesToStageReference.get()[0];
    assertTrue(stagesToStageReferenceResolver instanceof StagesReferenceResolver);
    assertEquals(stageElementInStagesBlock, stagesToStageReferenceResolver.getElement());
    var jobTargets = ((StagesReferenceResolver) stagesToStageReferenceResolver).getTargets();
    assertTrue(jobTargets.containsAll(jobElements));
  }

}
