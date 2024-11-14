package com.github.deeepamin.gitlabciaid.services.resolvers;

import com.github.deeepamin.gitlabciaid.utils.FileUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class ScriptReferenceResolver extends PsiReferenceBase<PsiElement> {
  public ScriptReferenceResolver(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    final Project project = myElement.getProject();
    var elementText = myElement.getText();
    var scriptPathIndex = getShOrPyScript(elementText);
    var scriptPath = elementText;
    if (scriptPathIndex != null) {
      scriptPath = scriptPathIndex.path;
      setRangeInElement(new TextRange(scriptPathIndex.start, scriptPathIndex.end));
    }
    var localFileSystemPath = FileUtils.getVirtualFile(scriptPath, project).orElse(null);
    if (localFileSystemPath != null) {
      return PsiManager.getInstance(project).findFile(localFileSystemPath);
    }
    return null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return myElement.getText();
  }

  private ScriptPathIndex getShOrPyScript(String elementText) {
    String regex = "(\\./|/)\\S+\\.(sh|py)";
    Pattern pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(elementText);
    if (matcher.find()) {
      return new ScriptPathIndex(matcher.group(), matcher.start(), matcher.end());
    }
    return null;
  }

  private record ScriptPathIndex(String path, int start, int end) {
  }
}
