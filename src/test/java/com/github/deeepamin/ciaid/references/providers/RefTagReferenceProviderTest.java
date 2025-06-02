package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.RefTagReferenceResolver;

public class RefTagReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetRefTagReferences() {
    var refTagElement = findChildWithKey(psiYaml, ".is_not_schedule");
    assertNotNull(refTagElement);
    referenceProvider = new RefTagReferenceProvider(refTagElement);
    var refTagReference = referenceProvider.getElementReferences();
    assertNotNull(refTagReference);
    assertTrue(refTagReference.isPresent());
    assertEquals(1, refTagReference.get().length);
    assertTrue(refTagReference.get()[0] instanceof RefTagReferenceResolver);
    assertEquals(refTagElement, refTagReference.get()[0].getElement());

    var refTagKeysElement = findChildWithKey(psiYaml, "conditions");
    assertNotNull(refTagKeysElement);
    referenceProvider = new RefTagReferenceProvider(refTagKeysElement);
    var refTagKeysReference = referenceProvider.getElementReferences();
    assertNotNull(refTagKeysReference);
    assertTrue(refTagKeysReference.isPresent());
    assertEquals(1, refTagKeysReference.get().length);
    assertTrue(refTagKeysReference.get()[0] instanceof RefTagReferenceResolver);
    assertEquals(refTagKeysElement, refTagKeysReference.get()[0].getElement());
  }
}
