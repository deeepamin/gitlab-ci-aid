package com.github.deeepamin.ciaid.services.providers;

import com.github.deeepamin.ciaid.BaseTest;

public class CIAidYamlSchemaProviderTest extends BaseTest {
  private CIAidYamlSchemaProvider myProvider;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myProvider = new CIAidYamlSchemaProvider();
  }

  public void testSchemaFileIsAvailable() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, "");
    var schemaFile = myProvider.getSchemaFile();
    assertNotNull(schemaFile);
    var psiFile = myFixture.getFile();
    assertNotNull(psiFile);
    assertTrue(myProvider.isAvailable(psiFile.getVirtualFile()));
  }
}
