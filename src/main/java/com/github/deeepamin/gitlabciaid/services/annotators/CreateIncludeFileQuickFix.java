package com.github.deeepamin.gitlabciaid.services.annotators;

import com.github.deeepamin.gitlabciaid.GitlabCIAidBundle;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import org.jetbrains.annotations.NotNull;

public class CreateIncludeFileQuickFix extends CreateAndOpenFileQuickFix {
  @Override
  public @IntentionName @NotNull String getName() {
    return GitlabCIAidBundle.message("annotator.gitlabciaid.create-include-file");
  }

  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return GitlabCIAidBundle.message("annotator.gitlabciaid.create-include-file");
  }
}
