package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.ExtendsReferenceResolver;

public class ExtendsReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetReferencesExtendsToJob() {
    var extendsJobElement = findChildWithKey(psiYaml, ".extend-test");
    assertNotNull(extendsJobElement);
    referenceProvider = new ExtendsReferenceProvider(extendsJobElement);
    var referenceExtends = referenceProvider.getElementReferences();
    assertNotNull(referenceExtends);
    assertTrue(referenceExtends.isPresent());
    assertEquals(1, referenceExtends.get().length);
    assertTrue(referenceExtends.get()[0] instanceof ExtendsReferenceResolver);
    assertEquals(extendsJobElement, referenceExtends.get()[0].getElement());
  }
}
