package com.github.deeepamin.ciaid.schema;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class JobAnchorSchemaSupport {
  private static final String HIDDEN_KEY_PATTERN = "^[.]";

  private JobAnchorSchemaSupport() {
  }

  public static String addJobAnchorSchema(String schemaText) {
    var schema = JsonParser.parseString(schemaText).getAsJsonObject();
    var definitions = schema.getAsJsonObject("definitions");
    var jobProperties = definitions.getAsJsonObject("job_template").getAsJsonObject("properties");

    var jobKeywordCondition = new JsonObject();
    jobKeywordCondition.addProperty("type", "object");
    var anyJobKeyword = new JsonArray();
    jobProperties.keySet().forEach(keyword -> {
      var requiredKeyword = new JsonObject();
      var required = new JsonArray();
      required.add(keyword);
      requiredKeyword.add("required", required);
      anyJobKeyword.add(requiredKeyword);
    });
    jobKeywordCondition.add("anyOf", anyJobKeyword);

    var jobTemplate = new JsonObject();
    jobTemplate.addProperty("$ref", "#/definitions/job_template");

    var arbitraryAnchor = new JsonObject();
    arbitraryAnchor.addProperty("description", "Arbitrary YAML anchor.");

    var hiddenKeySchema = schema.getAsJsonObject("patternProperties").getAsJsonObject(HIDDEN_KEY_PATTERN);
    hiddenKeySchema.add("if", jobKeywordCondition);
    hiddenKeySchema.add("then", jobTemplate);
    hiddenKeySchema.add("else", arbitraryAnchor);
    return schema.toString();
  }
}
