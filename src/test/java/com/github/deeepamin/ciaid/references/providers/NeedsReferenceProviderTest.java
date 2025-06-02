package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.NeedsReferenceResolver;

public class NeedsReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetReferencesNeedsToJob() {
    var needsJobElement = findChildWithKey(psiYaml, "\"test-job\"");
    assertNotNull(needsJobElement);
    referenceProvider = new NeedsReferenceProvider(needsJobElement);
    var referenceNeeds = referenceProvider.getElementReferences();
    assertNotNull(referenceNeeds);
    assertTrue(referenceNeeds.isPresent());
    assertEquals(1, referenceNeeds.get().length);
    assertTrue(referenceNeeds.get()[0] instanceof NeedsReferenceResolver);
    assertEquals(needsJobElement, referenceNeeds.get()[0].getElement());
  }
}
