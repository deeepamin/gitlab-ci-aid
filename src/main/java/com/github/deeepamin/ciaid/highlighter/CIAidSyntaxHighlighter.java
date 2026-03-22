package com.github.deeepamin.ciaid.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

/**
 * Delegates to the standard YAML syntax highlighter.
 * Custom highlighting is performed by {@link CIAidYamlAnnotator}.
 */
public class CIAidSyntaxHighlighter implements SyntaxHighlighter {
  private final SyntaxHighlighter yamlHighlighter =
          SyntaxHighlighterFactory.getSyntaxHighlighter(YAMLLanguage.INSTANCE, null, null);

  @Override
  public @NotNull Lexer getHighlightingLexer() {
    return yamlHighlighter.getHighlightingLexer();
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    return yamlHighlighter.getTokenHighlights(tokenType);
  }
}
