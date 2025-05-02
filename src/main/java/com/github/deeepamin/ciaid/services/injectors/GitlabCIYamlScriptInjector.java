package com.github.deeepamin.ciaid.services.injectors;

import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.sh.ShLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalarList;
import org.jetbrains.yaml.psi.YAMLScalarText;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.List;

import static com.github.deeepamin.ciaid.model.GitlabCIYamlKeywords.SCRIPT_KEYWORDS;

public class GitlabCIYamlScriptInjector implements MultiHostInjector {
  private static final Logger LOG = Logger.getInstance(GitlabCIYamlScriptInjector.class);

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (GitlabCIYamlUtils.getGitlabCIYamlFile(context).isEmpty()) {
      LOG.debug(String.format("%s is not an element in Gitlab CI Yaml.", context.getText()));
      return;
    }
    if (context instanceof YAMLPlainTextImpl) {
      var parent = context.getParent();
      if (parent instanceof YAMLKeyValue) {
        LOG.debug(String.format("Script %s is not sequence item, skipping injection", context.getText()));
        return;
      }
      injectShell(registrar, context);
    }
    if (context instanceof YAMLScalarList || context instanceof YAMLScalarText) {
      injectShell(registrar, context);
    }
  }

  private void injectShell(MultiHostRegistrar registrar, PsiElement context) {
    var isScript = PsiUtils.isChild(context, SCRIPT_KEYWORDS);
    if (isScript) {
      registrar
              .startInjecting(ShLanguage.INSTANCE)
              .addPlace(null, null, (PsiLanguageInjectionHost) context, new TextRange(0, context.getTextLength()))
              .doneInjecting();
    }
  }

  @Override
  public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return List.of(YAMLPlainTextImpl.class, YAMLScalarList.class, YAMLScalarText.class);
  }
}
