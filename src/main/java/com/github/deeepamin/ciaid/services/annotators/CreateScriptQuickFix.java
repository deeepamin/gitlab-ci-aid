package com.github.deeepamin.ciaid.services.annotators;

import com.github.deeepamin.ciaid.GitlabCIAidBundle;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import org.jetbrains.annotations.NotNull;

public class CreateScriptQuickFix extends CreateAndOpenFileQuickFix {
  @Override
  public @IntentionName @NotNull String getName() {
    return GitlabCIAidBundle.message("annotator.gitlabciaid.create-script");
  }

  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return GitlabCIAidBundle.message("annotator.gitlabciaid.create-script");
  }
}
