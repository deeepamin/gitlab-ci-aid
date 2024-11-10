package com.github.deeepamin.gitlabciaid.services.injectors;

import com.github.deeepamin.gitlabciaid.utils.PsiUtils;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.sh.ShLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.List;

import static com.github.deeepamin.gitlabciaid.model.GitlabCIYamlKeywords.SCRIPT_KEYWORDS;

public class GitlabCIYamlScriptInjector implements MultiHostInjector {
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlScriptInjector.class);

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (context instanceof YAMLPlainTextImpl) {
      var parent = context.getParent();
      if (parent instanceof YAMLKeyValue) {
        LOG.debug(String.format("Script %s is not sequence item, skipping injection", context.getText()));
        return;
      }
      var isScript = PsiUtils.isChild(context, SCRIPT_KEYWORDS);
      if (isScript) {
        registrar.startInjecting(ShLanguage.INSTANCE)
                .addPlace(null, null, (PsiLanguageInjectionHost) context, new TextRange(0, context.getTextLength()))
                .doneInjecting();
      }
    }
  }

  @Override
  public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return List.of(YAMLPlainTextImpl.class);
  }
}
