package com.github.deeepamin.gitlabciaid.services.providers;

import com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.SchemaType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

public class GitlabCIYamlSchemaProvider implements JsonSchemaFileProvider {
  private static final String SCHEMA_NAME = "Gitlab CI";
  private static final String SCHEMA_PATH = "/schemas/gitlab-ci-yml.json";
  private final VirtualFile schemaFile;

  public GitlabCIYamlSchemaProvider() {
    this.schemaFile = Optional.ofNullable(getClass().getResourceAsStream(SCHEMA_PATH))
            .map(schemaStream -> {
              try (final Scanner scanner = new Scanner(schemaStream, StandardCharsets.UTF_8)) {
                final String schemaContent = scanner.useDelimiter("\\A").next();
                return new LightVirtualFile("gitlab-ci-yml.json", JsonFileType.INSTANCE, schemaContent);
              }
            })
            .orElse(null);
  }

  @Override
  public boolean isAvailable(@NotNull VirtualFile virtualFile) {
    if (virtualFile instanceof VirtualFileWindow) {
      virtualFile = ((VirtualFileWindow) virtualFile).getDelegate();
    }
    return virtualFile.isValid() && virtualFile.exists() && GitlabCIYamlUtils.isGitlabCIYamlFile(virtualFile.toNioPath());
  }

  @Override
  public @NotNull @Nls String getName() {
    return SCHEMA_NAME;
  }

  @Override
  public @Nullable VirtualFile getSchemaFile() {
    return schemaFile;
  }

  @Override
  public @NotNull SchemaType getSchemaType() {
    return SchemaType.schema;
  }
}
