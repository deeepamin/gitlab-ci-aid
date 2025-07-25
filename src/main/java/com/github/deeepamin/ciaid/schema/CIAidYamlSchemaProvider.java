package com.github.deeepamin.ciaid.schema;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.application.ReadAction;
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

import java.util.Optional;

public class CIAidYamlSchemaProvider implements JsonSchemaFileProvider {
  private static final Logger LOG = Logger.getInstance(CIAidYamlSchemaProvider.class);
  private static final String SCHEMA_NAME = "Gitlab CI [Auto]";
  private static final String SCHEMA_PATH = "/schemas/gitlab-ci-yml.json";
  private VirtualFile schemaFile;

  @Override
  public boolean isAvailable(@NotNull VirtualFile virtualFile) {
    if (virtualFile instanceof VirtualFileWindow) {
      LOG.debug("VirtualFileWindow");
      virtualFile = ((VirtualFileWindow) virtualFile).getDelegate();
    }
    if (virtualFile instanceof LightVirtualFile) {
      LOG.debug("LightVirtualFile" + virtualFile.getPath());
    }
    return CIAidProjectService.isValidGitlabCIYamlFile(virtualFile);
  }

  @Override
  public @NotNull @Nls String getName() {
    return SCHEMA_NAME;
  }

  @Override
  public @Nullable VirtualFile getSchemaFile() {
    return readSchemaFile();
  }

  @Override
  public @NotNull SchemaType getSchemaType() {
    return SchemaType.schema;
  }

  @Override
  public JsonSchemaVersion getSchemaVersion() {
    return JsonSchemaVersion.SCHEMA_7;
  }

  private VirtualFile readSchemaFile() {
    if (schemaFile != null && schemaFile.isValid()) {
      return schemaFile;
    }

    this.schemaFile = ReadAction.compute(() -> Optional.ofNullable(getClass().getResource(SCHEMA_PATH))
            .map(VfsUtil::findFileByURL)
            .orElse(null));
    return schemaFile;
  }
}
