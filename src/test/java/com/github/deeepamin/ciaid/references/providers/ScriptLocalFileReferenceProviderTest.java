package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.ScriptReferenceResolver;

public class ScriptLocalFileReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetReferencesScript() {
    var deployScriptElement = findChildWithKey(psiYaml, "./deploy.sh");
    assertNotNull(deployScriptElement);
    referenceProvider = new ScriptLocalFileReferenceProvider(deployScriptElement);
    var reference = referenceProvider.getElementReferences();
    assertNotNull(reference);
    assertTrue(reference.isPresent());
    assertEquals(1, reference.get().length);
    assertTrue(reference.get()[0] instanceof ScriptReferenceResolver);
    assertEquals(deployScriptElement, reference.get()[0].getElement());
  }

}
