package com.github.deeepamin.ciaid.lineMarkers;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

/**
 * Provides line markers for job properties that override parent job properties through extends inheritance.
 * Shows an "Overrides" icon in the gutter that allows navigation to the parent job's property.
 */
public class CIAidJobOverrideLineMarkerProvider implements LineMarkerProvider {

  private static final List<String> OVERRIDABLE_KEYWORDS = List.of(
          AFTER_SCRIPT,
          BEFORE_SCRIPT,
          SCRIPT,
          CACHE,
          IMAGE,
          SERVICES,
          VARIABLES,
          ARTIFACTS,
          STAGE,
          NEEDS,
          DEPENDENCIES,
          RULES,
          INTERRUPTIBLE,
          TAGS
  );

  @Override
  public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
    // Only process YAMLKeyValue elements
    if (!(element instanceof YAMLKeyValue keyValue) || keyValue.getKey() == null) {
      return null;
    }
    var isGitLabCIYaml = CIAidProjectService.hasGitlabYamlFile(keyValue);
    if (!isGitLabCIYaml) {
      return null;
    }

    String keyText = keyValue.getKeyText();

    // Check if this is an overridable keyword
    if (!OVERRIDABLE_KEYWORDS.contains(keyText)) {
      return null;
    }

    Optional<YAMLKeyValue> parentJobOpt = getParentJob(element);
    if (parentJobOpt.isEmpty()) {
      return null;
    }

    YAMLKeyValue parentJob = parentJobOpt.get();

    List<String> extendsChain = getExtendsChain(parentJob);
    if (extendsChain.isEmpty()) {
      return null;
    }

    Optional<YAMLKeyValue> overriddenPropertyOpt = findPropertyInExtendsChain(keyText, extendsChain, element);
    if (overriddenPropertyOpt.isEmpty()) {
      return null;
    }

    YAMLKeyValue overriddenProperty = overriddenPropertyOpt.get();

    // Get the job name that contains the overridden property
    Optional<YAMLKeyValue> overriddenJobOpt = getParentJob(overriddenProperty);
    String jobName = overriddenJobOpt.map(YAMLKeyValue::getKeyText).orElse("parent");

    // Create the line marker
    return NavigationGutterIconBuilder
            .create(AllIcons.Gutter.OverridingMethod)
            .setTarget(overriddenProperty)
            .setTooltipText("Overrides '" + keyText + "' from '" + jobName + "'")
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .createLineMarkerInfo(keyValue.getKey());
  }

  private Optional<YAMLKeyValue> getParentJob(PsiElement element) {
    var projectService = CIAidProjectService.getInstance(element.getProject());
    var allJobs = projectService.getDataProvider().getJobNames();
    return PsiUtils.findParent(element, allJobs);
  }

  private List<String> getExtendsChain(YAMLKeyValue job) {
    List<String> chain = new ArrayList<>();
    YAMLKeyValue currentJob = job;

    // Prevent infinite loops
    int maxDepth = 10;
    int depth = 0;

    while (currentJob != null && depth < maxDepth) {
      // Find the extends property in the current job
      var jobValue = currentJob.getValue();
      if (!(jobValue instanceof YAMLMapping jobMapping)) {
        continue;
      }

      YAMLKeyValue extendsKeyValue = jobMapping.getKeyValueByKey(EXTENDS);
      if (extendsKeyValue == null) {
        break;
      }

      String extendsValue = handleQuotedText(extendsKeyValue.getValueText());
      if (extendsValue.isEmpty() || chain.contains(extendsValue)) {
        break; // Prevent circular references
      }

      chain.add(extendsValue);

      // Find the next job in the chain
      currentJob = findJobByName(extendsValue, currentJob);
      depth++;
    }

    return chain;
  }

  private YAMLKeyValue findJobByName(String jobName, PsiElement context) {
    var projectService = CIAidProjectService.getInstance(context.getProject());
    return projectService.getDataProvider()
            .getPluginData()
            .values()
            .stream()
            .flatMap(yamlData -> yamlData.getJobElements().stream())
            .filter(job -> handleQuotedText(job.getKeyText()).equals(jobName))
            .findFirst()
            .orElse(null);
  }

  private Optional<YAMLKeyValue> findPropertyInExtendsChain(String propertyName, List<String> extendsChain, PsiElement context) {
    for (String jobName : extendsChain) {
      YAMLKeyValue job = findJobByName(jobName, context);
      if (job == null) {
        continue;
      }

      var jobValue = job.getValue();
      if (!(jobValue instanceof YAMLMapping jobMapping)) {
        continue;
      }

      YAMLKeyValue property = jobMapping.getKeyValueByKey(propertyName);
      if (property != null) {
        return Optional.of(property);
      }
    }

    return Optional.empty();
  }
}

