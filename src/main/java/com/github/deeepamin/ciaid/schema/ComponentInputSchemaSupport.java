package com.github.deeepamin.ciaid.schema;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ComponentInputSchemaSupport {
  private static final String INPUT_PATTERN = "^\\$\\[\\[\\s*inputs\\.[\\w-]+\\s*]]$";

  private ComponentInputSchemaSupport() {
  }

  public static String addComponentInputExpressions(String schemaText) {
    var schema = JsonParser.parseString(schemaText).getAsJsonObject();
    var definitions = schema.getAsJsonObject("definitions");
    var expression = inputExpressionSchema();

    allowExpressionInTypedSchema(definitions.getAsJsonObject("rules"), "array", "null");
    allowExpressionInTypedSchema(definitions.getAsJsonObject("tags"), "array");

    var jobProperties = definitions.getAsJsonObject("job_template").getAsJsonObject("properties");
    addExpressionAlternative(jobProperties.getAsJsonObject("stage").getAsJsonArray("anyOf"), expression);
    allowExpressionInTypedSchema(jobProperties.getAsJsonObject("needs"), "array");
    allowExpressionForOptionalNeeds(jobProperties.getAsJsonObject("needs"));
    allowExpressionForOptionalNeeds(definitions.getAsJsonObject("rulesNeeds"));

    return schema.toString();
  }

  private static JsonObject inputExpressionSchema() {
    var expression = new JsonObject();
    expression.addProperty("type", "string");
    expression.addProperty("pattern", INPUT_PATTERN);
    return expression;
  }

  private static void allowExpressionInTypedSchema(JsonObject schema, String... allowedTypes) {
    var types = new JsonArray();
    for (var allowedType : allowedTypes) {
      types.add(allowedType);
    }
    types.add("string");
    schema.add("type", types);
    schema.addProperty("pattern", INPUT_PATTERN);
  }

  private static void addExpressionAlternative(JsonArray alternatives, JsonObject expression) {
    alternatives.add(expression.deepCopy());
  }

  private static void allowExpressionForOptionalNeeds(JsonObject needsSchema) {
    var items = needsSchema.getAsJsonObject("items");
    for (var alternative : items.getAsJsonArray("oneOf")) {
      if (!alternative.isJsonObject()) {
        continue;
      }
      var properties = alternative.getAsJsonObject().getAsJsonObject("properties");
      if (properties == null || !properties.has("optional")) {
        continue;
      }
      var optional = new JsonObject();
      var alternatives = new JsonArray();
      alternatives.add(properties.get("optional").deepCopy());
      alternatives.add(inputExpressionSchema());
      optional.add("anyOf", alternatives);
      properties.add("optional", optional);
    }
  }
}
