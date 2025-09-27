package com.github.deeepamin.ciaid.utils;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.SystemProperties;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JLabel;
import javax.swing.plaf.LabelUI;
import java.net.URI;
import java.net.URL;
import java.util.function.Supplier;

public class CIAidUtils {
  public static TextRange getHighlightTextRange(PsiElement element, int start, int end) {
    var elementTextRange = element.getTextRange();
    var highlightStartRange = elementTextRange.getStartOffset() + start;
    if (highlightStartRange > elementTextRange.getEndOffset()) {
      highlightStartRange = elementTextRange.getStartOffset();
    }
    var highlightEndRange = elementTextRange.getStartOffset() + end;
    if (highlightEndRange > elementTextRange.getEndOffset()) {
      highlightEndRange = elementTextRange.getEndOffset();
    }
    return new TextRange(highlightStartRange, highlightEndRange);
  }

  public static boolean isValidUrl(String input) {
    if (input == null) {
      return false;
    }
    if (input.isBlank()) {
      // allow empty URLs for default value
      return true;
    }
    try {
      URL url = URI.create(input).toURL();
      return url.getProtocol().equals("http") || url.getProtocol().equals("https");
    } catch (Exception e) {
      return false;
    }
  }

  public static String handleQuotedText(String text) {
    if (text.startsWith("\"") && text.endsWith("\"")) {
      text = text.replaceAll("\"", "");
    } else if (text.startsWith("'") && text.endsWith("'")) {
      text = text.replaceAll("'", "");
    }
    return text;
  }

  public static boolean containsWildcardWithYmlExtension(String path) {
    return containsWildcard(path) && (path.endsWith(".yml") || path.endsWith(".yaml"));
  }

  public static boolean containsWildcard(String path) {
    return path != null && path.contains("*");
  }

  public static @NotNull JLabel createCommentComponent(@NlsContexts.DetailedDescription @Nullable String commentText,
                                                       boolean isCommentBelow) {
    return createCommentComponent(commentText, isCommentBelow, 70, true);
  }

  public static @NotNull JLabel createCommentComponent(@NlsContexts.DetailedDescription @Nullable String commentText,
                                                       boolean isCommentBelow, int maxLineLength, boolean allowAutoWrapping) {
    return createCommentComponent(() -> new CommentLabel(""), commentText, isCommentBelow, maxLineLength, allowAutoWrapping);
  }

  private static JLabel createCommentComponent(@NotNull Supplier<? extends JBLabel> labelSupplier,
                                               @NlsContexts.DetailedDescription @Nullable String commentText,
                                               boolean isCommentBelow, int maxLineLength, boolean allowAutoWrapping) {
    boolean isCopyable = SystemProperties.getBooleanProperty("idea.ui.comment.copyable", true);
    JLabel component = labelSupplier.get().setCopyable(isCopyable).setAllowAutoWrapping(allowAutoWrapping);
    component.setVerticalTextPosition(1);
    component.setFocusable(false);
    if (isCopyable) {
      setCommentText(component, commentText, isCommentBelow, maxLineLength);
    } else {
      component.setText(commentText);
    }
    return component;
  }

  private static void setCommentText(@NotNull JLabel component, @Nullable @NlsContexts.DetailedDescription String commentText,
                                     boolean isCommentBelow, int maxLineLength) {
    if (commentText != null) {
      String css = "<head><style type=\"text/css\">\na, a:link {color:#"
              + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.ENABLED)
              + ";}\na:visited {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.VISITED)
              + ";}\na:hover {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.HOVERED)
              + ";}\na:active {color:#" + ColorUtil.toHex(JBUI.CurrentTheme.Link.Foreground.PRESSED)
              + ";}\n</style>\n</head>";
      HtmlChunk text = HtmlChunk.raw(commentText);
      HtmlChunk.Element element;
      if (maxLineLength > 0 && commentText.length() > maxLineLength && isCommentBelow) {
        int width = component.getFontMetrics(component.getFont()).stringWidth(commentText.substring(0, maxLineLength));
        element = text.wrapWith(HtmlChunk.div().attr("width", width));
      } else {
        element = text.wrapWith(HtmlChunk.div());
      }
      component.setText((new HtmlBuilder()).append(HtmlChunk.raw(css))
              .append(element.wrapWith("body"))
              .wrapWith("html")
              .toString());
    }
  }

  private static class CommentLabel extends JBLabel {
    private CommentLabel(@NlsContexts.Label @NotNull String text) {
      super(text);
      this.setForeground(JBUI.CurrentTheme.ContextHelp.FOREGROUND);
    }

    public void setUI(LabelUI ui) {
      super.setUI(ui);
      this.setFont(JBFont.medium());
    }
  }
}
