package com.github.deeepamin.gitlabciaid.services.providers;

import com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.SchemaType;
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

public class GitlabCIYamlSchemaProvider implements JsonSchemaFileProvider {
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlSchemaProvider.class);
  private static final String SCHEMA_NAME = "Gitlab CI [Auto]";
  private static final String SCHEMA_PATH = "/schemas/gitlab-ci-yml.json";
  private final VirtualFile schemaFile;

  public GitlabCIYamlSchemaProvider() {
    this.schemaFile = Optional.ofNullable(getClass().getResource(SCHEMA_PATH))
            .map(VfsUtil::findFileByURL)
            .orElse(null);
  }

  @Override
  public boolean isAvailable(@NotNull VirtualFile virtualFile) {
    if (virtualFile instanceof VirtualFileWindow) {
      LOG.debug("VirtualFileWindow");
      virtualFile = ((VirtualFileWindow) virtualFile).getDelegate();
    }
    if (virtualFile instanceof LightVirtualFile) {
      LOG.debug("LightVirtualFile" + virtualFile.getPath());
    }

    if (virtualFile.isValid() && virtualFile.exists()) {
      Path virtualFilePath;
      try {
        virtualFilePath = Path.of(virtualFile.getPath());
      } catch (InvalidPathException ipX) {
        LOG.error("Error while JSON schema availability check for path " + virtualFile.getPath() , ipX);
        return false;
      }
      return GitlabCIYamlUtils.isGitlabCIYamlFile(virtualFilePath);
    }
    return false;
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

  @Override
  public JsonSchemaVersion getSchemaVersion() {
    return JsonSchemaVersion.SCHEMA_7;
  }
}
