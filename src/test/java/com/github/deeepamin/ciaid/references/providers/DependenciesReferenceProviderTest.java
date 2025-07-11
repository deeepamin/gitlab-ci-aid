package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.DependenciesReferenceResolver;

public class DependenciesReferenceProviderTest extends BaseReferenceProviderTest {

  public void testGetReferencesDependenciesToJob() {
    var dependenciesElement = findChildWithKey(psiYaml, "deploy-job");
    assertNotNull(dependenciesElement);
    referenceProvider = new DependenciesReferenceProvider(dependenciesElement);
    var referenceDependencies = referenceProvider.getElementReferences();
    assertNotNull(referenceDependencies);
    assertTrue(referenceDependencies.isPresent());
    assertEquals(1, referenceDependencies.get().length);
    assertTrue(referenceDependencies.get()[0] instanceof DependenciesReferenceResolver);
    assertEquals(dependenciesElement, referenceDependencies.get()[0].getElement());
  }
}
