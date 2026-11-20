package com.github.deeepamin.ciaid.services.providers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.schema.CIAidYamlSchemaProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;

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
    try {
      var schemaText = new String(schemaFile.contentsToByteArray(), StandardCharsets.UTF_8);
      var definitions = JsonParser.parseString(schemaText).getAsJsonObject().getAsJsonObject("definitions");
      var tags = definitions.getAsJsonObject("tags");
      assertEquals("array", tags.getAsJsonArray("type").get(0).getAsString());
      assertEquals("string", tags.getAsJsonArray("type").get(1).getAsString());
      assertEquals("^\\$\\[\\[\\s*inputs\\.[\\w-]+\\s*]]$", tags.get("pattern").getAsString());
      assertTrue(tags.has("items"));

      var jobProperties = definitions.getAsJsonObject("job_template").getAsJsonObject("properties");
      var stageAlternatives = jobProperties.getAsJsonObject("stage").getAsJsonArray("anyOf");
      assertTrue(stageAlternatives.asList().stream()
              .anyMatch(alternative -> alternative.getAsJsonObject().has("pattern")));

      var rules = definitions.getAsJsonObject("rules");
      assertEquals("array", rules.getAsJsonArray("type").get(0).getAsString());
      assertEquals("string", rules.getAsJsonArray("type").get(2).getAsString());
      assertTrue(rules.has("items"));

      var needs = jobProperties.getAsJsonObject("needs");
      assertEquals("array", needs.getAsJsonArray("type").get(0).getAsString());
      var needsAlternatives = needs.getAsJsonObject("items").getAsJsonArray("oneOf");
      assertTrue(needsAlternatives.size() > 1);
      assertTrue(needsAlternatives.asList().stream()
              .filter(JsonElement::isJsonObject)
              .filter(alternative -> alternative.getAsJsonObject().has("properties"))
              .map(alternative -> alternative.getAsJsonObject().getAsJsonObject("properties"))
              .filter(properties -> properties.has("optional"))
              .anyMatch(properties -> properties.getAsJsonObject("optional").has("anyOf")));
    } catch (java.io.IOException exception) {
      fail(exception.getMessage());
    }
  }
}
