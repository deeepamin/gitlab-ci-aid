package com.github.deeepamin.ciaid;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

public class CIAidBundle extends DynamicBundle {
  @NonNls
  private static final String BUNDLE_PATH = "messages.CIAid";
  private static final CIAidBundle INSTANCE = new CIAidBundle();

  protected CIAidBundle() {
    super(BUNDLE_PATH);
  }

  public static @Nls String message(@PropertyKey(resourceBundle = BUNDLE_PATH) String key, Object... params) {
    return INSTANCE.getMessage(key, params);
  }
}
