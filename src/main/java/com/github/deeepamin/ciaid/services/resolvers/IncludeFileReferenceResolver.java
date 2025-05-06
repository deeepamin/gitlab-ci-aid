package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.ReferenceUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class IncludeFileReferenceResolver extends PsiReferenceBase<PsiElement> {
  private static final Logger LOG = Logger.getInstance(IncludeFileReferenceResolver.class);

  public IncludeFileReferenceResolver(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    final Project project = myElement.getProject();
    var text = ReferenceUtils.handleQuotedText(myElement.getText());
    Path filePath = null;
    try {
      filePath = FileUtils.getFilePath(text, project);
    } catch (InvalidPathException ipX) {
      LOG.error("Error while getting file path " + filePath, ipX);
    }
    if (filePath != null) {
      var localFileSystemPath = LocalFileSystem.getInstance().findFileByPath(filePath.toString());
      if (localFileSystemPath != null) {
        return PsiManager.getInstance(project).findFile(localFileSystemPath);
      }
    }
    // fallback to Intellij Filesystem Global search in case the path doesn't resolve to a file in the project
    var localFileSystemPath = FileUtils.findVirtualFile(text, project).orElse(null);
    if (localFileSystemPath != null) {
      return PsiManager.getInstance(project).findFile(localFileSystemPath);
    }
    return null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return ReferenceUtils.handleQuotedText(myElement.getText());
  }
}
