package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.InputsReferenceResolver;

public class InputsReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetInputReferences() {
    var inputElement = findChildWithKey(psiYaml, "$[[ inputs.context ]]");
    assertNotNull(inputElement);
    referenceProvider = new InputsReferenceProvider(inputElement);
    var inputReference = referenceProvider.getElementReferences();
    assertNotNull(inputReference);
    assertTrue(inputReference.isPresent());
    assertEquals(1, inputReference.get().length);
    assertTrue(inputReference.get()[0] instanceof InputsReferenceResolver);
    assertEquals(inputElement, inputReference.get()[0].getElement());
  }
}
