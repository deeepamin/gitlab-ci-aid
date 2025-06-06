package com.github.deeepamin.ciaid.schema;

import com.intellij.openapi.project.Project;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CIAidYamlSchemaProviderFactory implements JsonSchemaProviderFactory {
  private static final List<JsonSchemaFileProvider> SCHEMA_FILE_PROVIDERS = List.of(new CIAidYamlSchemaProvider());

  @Override
  public @NotNull List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
    return SCHEMA_FILE_PROVIDERS;
  }
}
