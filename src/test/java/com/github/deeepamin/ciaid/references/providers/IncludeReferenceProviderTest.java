package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.IncludeFileReferenceResolver;

public class IncludeReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetReferencesIncludeFile() {
    var includeFileElement = findChildWithKey(psiYaml, "/pipeline.yml");
    assertNotNull(includeFileElement);
    referenceProvider = new IncludeReferenceProvider(includeFileElement);
    var referenceIncludeFile = referenceProvider.getElementReferences();
    assertNotNull(referenceIncludeFile);
    assertTrue(referenceIncludeFile.isPresent());
    assertEquals(1, referenceIncludeFile.get().length);
    assertTrue(referenceIncludeFile.get()[0] instanceof IncludeFileReferenceResolver);
    assertEquals(includeFileElement, referenceIncludeFile.get()[0].getElement());
  }
}
