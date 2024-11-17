package com.github.deeepamin.gitlabciaid.language;

import com.github.deeepamin.gitlabciaid.utils.GitlabCIYamlUtils;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class GitlabCIYamlFileTypeOverrider implements FileTypeOverrider {
  @Override
  public @Nullable FileType getOverriddenFileType(@NotNull VirtualFile virtualFile) {
    if (GitlabCIYamlUtils.isValidGitlabCIYamlFile(virtualFile)) {
      return new GitlabCIYamlFileType();
    }
    return null;
  }
}
