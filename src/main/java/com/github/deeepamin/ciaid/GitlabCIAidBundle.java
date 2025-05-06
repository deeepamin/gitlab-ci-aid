package com.github.deeepamin.ciaid;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

public class GitlabCIAidBundle extends DynamicBundle {
  @NonNls
  private static final String BUNDLE_PATH = "messages.GitlabCIAid";
  private static final GitlabCIAidBundle INSTANCE = new GitlabCIAidBundle();

  protected GitlabCIAidBundle() {
    super(BUNDLE_PATH);
  }

  public static @Nls String message(@PropertyKey(resourceBundle = BUNDLE_PATH) String key, Object... params) {
    return INSTANCE.getMessage(key, params);
  }
}
