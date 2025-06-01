package com.github.deeepamin.ciaid.services.annotators;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import org.jetbrains.annotations.NotNull;

public class CreateScriptQuickFix extends CreateAndOpenFileQuickFix {
  @Override
  public @IntentionName @NotNull String getName() {
    return CIAidBundle.message("inspections.gitlab.ci.fix.create-script");
  }

  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return CIAidBundle.message("inspections.gitlab.ci.fix.create-script");
  }
}
