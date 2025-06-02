package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.utils.FileUtils;

import java.util.List;

import static com.github.deeepamin.ciaid.references.providers.VariablesReferenceProvider.getVariables;

public class VariablesReferenceProviderTest extends BaseTest {

  public void testGetVariables() {
    assertEquals("test", getVariables("$test").getFirst().path());
    assertEquals("test", getVariables("${test}").getFirst().path());
    assertTrue(List.of("test", "test2").containsAll(getVariables("$test ${test2}").stream().map(FileUtils.StringWithStartEndRange::path).toList()));
  }
}
