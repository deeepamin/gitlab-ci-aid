package com.github.deeepamin.ciaid.highlighter;

import com.github.deeepamin.ciaid.CIAidBundle;
import com.github.deeepamin.ciaid.model.Icons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Map;

public class CIAidColorSettingsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
          new AttributesDescriptor(CIAidBundle.message("color.settings.stage"), CIAidTextAttributes.STAGE),
          new AttributesDescriptor(CIAidBundle.message("color.settings.job"), CIAidTextAttributes.JOB),
          new AttributesDescriptor(CIAidBundle.message("color.settings.script.path"), CIAidTextAttributes.SCRIPT_PATH),
          new AttributesDescriptor(CIAidBundle.message("color.settings.include"), CIAidTextAttributes.INCLUDE),
          new AttributesDescriptor(CIAidBundle.message("color.settings.inputs"), CIAidTextAttributes.INPUTS),
  };

  private static final Map<String, TextAttributesKey> ADDITIONAL_HIGHLIGHTING_TAG_TO_DESCRIPTOR_MAP = Map.of(
          "stage", CIAidTextAttributes.STAGE,
          "job", CIAidTextAttributes.JOB,
          "script_path", CIAidTextAttributes.SCRIPT_PATH,
          "include", CIAidTextAttributes.INCLUDE,
          "inputs", CIAidTextAttributes.INPUTS
  );

  @Override
  public @Nullable Icon getIcon() {
    return Icons.ICON_GITLAB_LOGO.getIcon();
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new CIAidSyntaxHighlighter();
  }

  @Override
  public @NonNls @NotNull String getDemoText() {
    return """
            stages:
              - <stage>build</stage>
              - <stage>test</stage>
              - <stage>deploy</stage>

            include:
              - <include>ci/templates/build.gitlab-ci.yml</include>
              - project: my-group/my-project
                file: <include>/templates/.gitlab-ci-template.yml</include>

            <job>build-job</job>:
              stage: <stage>build</stage>
              image: gradle:latest
              script:
                - <script_path>./scripts/build.sh</script_path>
                - echo "Building $<inputs>$[[ inputs.environment ]]</inputs>"

            <job>test-job</job>:
              stage: <stage>test</stage>
              needs:
                - <job>build-job</job>
              script:
                - <script_path>./scripts/test.sh</script_path>

            <job>deploy-job</job>:
              stage: <stage>deploy</stage>
              needs:
                - <job>test-job</job>
              script:
                - <script_path>./scripts/deploy.sh</script_path> -e ENV=test
            """;
  }

  @Override
  public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ADDITIONAL_HIGHLIGHTING_TAG_TO_DESCRIPTOR_MAP;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getDisplayName() {
    return CIAidBundle.message("color.settings.display.name");
  }
}

