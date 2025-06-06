package com.github.deeepamin.ciaid.injectors;

import com.github.deeepamin.ciaid.references.providers.RefTagReferenceProvider;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.YAMLScalarList;
import org.jetbrains.yaml.psi.YAMLScalarText;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.List;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;

/**
 * Base class to allow injecting a shell script language into GitLab YAML elements.
 * To avoid a compile-time dependency on the language, the ID is passed as a string.
 */
abstract class AbstractCIAidYamlScriptInjector implements MultiHostInjector {
  private static final Logger LOG = Logger.getInstance(AbstractCIAidYamlScriptInjector.class);
  private final @Nullable Language language;

  protected AbstractCIAidYamlScriptInjector(@NotNull String languageId) {
    this.language = Language.findLanguageByID(languageId);
  }

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (language == null) {
      return;
    }

    if (!CIAidProjectService.hasGitlabYamlFile(context)) {
      LOG.debug(String.format("%s is not an element in Gitlab CI Yaml.", context.getText()));
      return;
    }
    if (context instanceof YAMLPlainTextImpl) {
      var parent = context.getParent();
      if (parent instanceof YAMLKeyValue) {
        LOG.debug(String.format("Script %s is not sequence item, skipping injection", context.getText()));
        return;
      }
      injectShell(registrar, context, language);
    }
    if (context instanceof YAMLScalarList) {
      // for | multi line blocks
      injectShell(registrar, context, language);
    } else if (context instanceof YAMLScalarText yamlScalarText) {
      // for > multi line blocks injection doesn't work fine due to the way text is handled
      var isFoldedLineBlock = isFoldedLineBlock(yamlScalarText);
      if (!isFoldedLineBlock) {
        injectShell(registrar, context, language);
      }
    }
  }

  private void injectShell(MultiHostRegistrar registrar, PsiElement context, @NotNull Language language) {
    if (!(context instanceof YAMLPsiElement yamlPsiElement)) {
      return;
    }
    var isScript = PsiUtils.isChild(context, SCRIPT_KEYWORDS);
    var isRefTag = RefTagReferenceProvider.getReferenceTag(yamlPsiElement) != null;
    if (isScript && !isRefTag) {
      registrar
              .startInjecting(language)
              .addPlace(null, null, (PsiLanguageInjectionHost) context, new TextRange(0, context.getTextLength()))
              .doneInjecting();
    }
  }

  private boolean isFoldedLineBlock(YAMLScalarText yamlScalarText) {
    return yamlScalarText != null && yamlScalarText.getText().startsWith(">");
  }

  @Override
  public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return List.of(YAMLPlainTextImpl.class, YAMLScalarList.class, YAMLScalarText.class);
  }
}
