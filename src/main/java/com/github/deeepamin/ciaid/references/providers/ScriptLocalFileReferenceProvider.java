package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.references.resolvers.ScriptReferenceResolver;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class ScriptLocalFileReferenceProvider extends AbstractReferenceProvider {
  // references to local files in script, after_script, before_script, e.g. "./script.sh" or `script: "file.py"
  public ScriptLocalFileReferenceProvider(PsiElement element) {
    super(element);
  }

  @Override
  protected boolean isReferenceAvailable() {
    var isYamlTextElement = YamlUtils.isYamlTextElement(element);
    var isYamlScalarListOrScalarTextElement = YamlUtils.isYamlScalarListOrYamlScalarTextElement(element);
    var isScriptElement = GitlabCIYamlUtils.isScriptElement(element);
    return isScriptElement && (isYamlTextElement || isYamlScalarListOrScalarTextElement);
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    var scriptText = handleQuotedText(element.getText());
    var scriptPathIndexes = FileUtils.getFilePathAndIndexes(scriptText);
    if (scriptPathIndexes.isEmpty()) {
      return Optional.of(PsiReference.EMPTY_ARRAY);
    }
    List<PsiReference> references = new ArrayList<>();
    for (var scriptPathIndex : scriptPathIndexes) {
      references.add(new ScriptReferenceResolver(element, new TextRange(scriptPathIndex.start(), scriptPathIndex.end())));
    }
    return Optional.of(references.toArray(new PsiReference[0]));
  }
}
