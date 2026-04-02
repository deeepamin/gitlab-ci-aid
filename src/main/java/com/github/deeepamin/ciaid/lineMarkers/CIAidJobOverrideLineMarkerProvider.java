package com.github.deeepamin.ciaid.lineMarkers;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

  private final ConcurrentHashMap<VirtualFile, LineMarkerCache> lineMarkerCache = new ConcurrentHashMap<>();

  @Override
  public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
    if (!(element instanceof YAMLKeyValue keyValue) || keyValue.getKey() == null) {
      return null;
    }

    String keyText = keyValue.getKeyText();
    if (!OVERRIDABLE_KEYWORDS.contains(keyText)) {
      return null;
    }

    var virtualFileOpt = CIAidProjectService.getGitlabCIYamlFile(element);
    if (virtualFileOpt.isEmpty()) {
      return null;
    }
    var virtualFile = virtualFileOpt.get();
    var cachedData = getLineMarkerCache(virtualFile);
    var projectService = CIAidProjectService.getInstance(element.getProject());
    var jobs = projectService.getDataProvider()
            .getPluginData()
            .values()
            .stream()
            .flatMap(yamlData -> yamlData.getJobElements().stream())
            .toList();
    var jobNames = jobs.stream()
            .map(job -> handleQuotedText(job.getKeyText()))
            .toList();

    var parentJobOpt = PsiUtils.findParent(element, jobNames);
    if (parentJobOpt.isEmpty()) {
      return null;
    }
    var parentJob = parentJobOpt.get();
    var parentJobName = handleQuotedText(parentJob.getKeyText());

    // Cache extends chain per job name
    var extendsChain = cachedData.extendsChains().computeIfAbsent(
            parentJobName,
            name -> buildExtendsChain(parentJob, jobs)
    );
    if (extendsChain.isEmpty()) {
      return null;
    }

    // Cache override property per "jobName:propertyName"
    var propertyCacheKey = parentJobName + ":" + keyText;
    var overriddenPropertyOpt = cachedData.properties().computeIfAbsent(
            propertyCacheKey,
            k -> findPropertyInExtendsChain(keyText, extendsChain, jobs)
    );
    if (overriddenPropertyOpt.isEmpty()) {
      return null;
    }

    var overriddenProperty = overriddenPropertyOpt.get();
    var overriddenJobOpt = PsiUtils.findParent(overriddenProperty, jobNames);

    var jobName = overriddenJobOpt.map(YAMLKeyValue::getKeyText).orElse(null);
    if (jobName == null) {
      return null;
    }

    return NavigationGutterIconBuilder
            .create(AllIcons.Gutter.OverridingMethod)
            .setTarget(overriddenProperty)
            .setTooltipText("Overrides '" + keyText + "' from '" + jobName + "'")
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .createLineMarkerInfo(keyValue.getKey());
  }

  private LineMarkerCache getLineMarkerCache(VirtualFile virtualFile) {
    var existing = lineMarkerCache.get(virtualFile);
    long currentStamp = virtualFile.getModificationStamp();
    if (existing != null && existing.modificationStamp() == currentStamp) {
      return existing;
    }
    var fresh = new LineMarkerCache(
            new ConcurrentHashMap<>(),
            new ConcurrentHashMap<>(),
            currentStamp
    );
    lineMarkerCache.put(virtualFile, fresh);
    return fresh;
  }

  private List<String> buildExtendsChain(YAMLKeyValue job, List<YAMLKeyValue> allJobs) {
    List<String> chain = new ArrayList<>();
    var current = job;
    int maxDepth = 10;
    int depth = 0;

    while (current != null && depth < maxDepth) {
      ProgressManager.checkCanceled();
      var jobValue = current.getValue();
      if (!(jobValue instanceof YAMLMapping jobMapping)) {
        break;
      }

      var extendsKeyValue = jobMapping.getKeyValueByKey(EXTENDS);
      if (extendsKeyValue == null) {
        break;
      }

      var extendsValue = handleQuotedText(extendsKeyValue.getValueText());
      if (extendsValue.isEmpty() || chain.contains(extendsValue)) {
        break; // prevent circular reference
      }

      chain.add(extendsValue);
      current = findJobByName(extendsValue, allJobs);
      depth++;
    }

    return chain;
  }

  private Optional<YAMLKeyValue> findPropertyInExtendsChain(String propertyName,
                                                             List<String> extendsChain,
                                                             List<YAMLKeyValue> allJobs) {
    for (String jobName : extendsChain) {
        ProgressManager.checkCanceled();
        var job = findJobByName(jobName, allJobs);
        if (job == null) continue;

        var jobValue = job.getValue();
        if (!(jobValue instanceof YAMLMapping jobMapping)) continue;

        var property = jobMapping.getKeyValueByKey(propertyName);
        if (property != null) {
            return Optional.of(property);
        }
    }
    return Optional.empty();
  }

  private YAMLKeyValue findJobByName(String jobName, List<YAMLKeyValue> allJobs) {
    return allJobs.stream()
            .filter(job -> handleQuotedText(job.getKeyText()).equals(jobName))
            .findFirst()
            .orElse(null);
  }

  private record LineMarkerCache(
          ConcurrentHashMap<String, List<String>> extendsChains,
          ConcurrentHashMap<String, Optional<YAMLKeyValue>> properties,
          long modificationStamp
  ) {}
}
