package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.InputsReferenceResolver;

import static com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider.getInputNames;
import static com.github.deeepamin.ciaid.references.providers.InputsReferenceProvider.getInputs;

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

  public void testGetInputs() {
    assertEquals("inputs.test", getInputs("$[[inputs.test]]").getFirst().path());
    assertEquals("inputs.test", getInputs("$[[ inputs.test ]]").getFirst().path());
    assertEquals("inputs.test", getInputs("$[[inputs.test ]]").getFirst().path());
    assertEquals("inputs.test", getInputs("$[[ inputs.test]]").getFirst().path());
    assertEquals("inputs.test", getInputs("   $[[ inputs.test ]]  ").getFirst().path());

    assertEmpty(getInputs("$[[ input.test ]]"));
    assertEmpty(getInputs("[[inputs.test ]]"));
    assertEmpty(getInputs("$[ inputs.test ]]"));
    assertEmpty(getInputs("$[[ inputs.test ]"));
    assertEmpty(getInputs("$$[ output.test ]]"));
  }

  public void testGetInputNameString() {
    assertEquals("test", getInputNames("$[[inputs.test]]").getFirst().path());
    assertEquals("test", getInputNames("$[[ inputs.test ]]").getFirst().path());
    assertEquals("test", getInputNames("$[[inputs.test ]]").getFirst().path());
    assertEquals("test", getInputNames("$[[ inputs.test]]").getFirst().path());
    assertEquals("test", getInputNames("   $[[ inputs.test ]]  ").getFirst().path());

    assertEmpty(getInputNames("$[[ input.test ]]"));
    assertEmpty(getInputNames("[[inputs.test ]]"));
    assertEmpty(getInputNames("$[ inputs.test ]]"));
    assertEmpty(getInputNames("$$[ output.test ]]"));
  }

}
