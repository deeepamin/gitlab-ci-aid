package com.github.deeepamin.ciaid.references.providers;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.cache.CIAidCacheUtils;
import com.github.deeepamin.ciaid.references.resolvers.IncludeFileReferenceResolver;
import com.github.deeepamin.ciaid.utils.GitlabCIYamlUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import java.util.List;
import java.util.Optional;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class IncludeReferenceProvider extends AbstractReferenceProvider {
  public IncludeReferenceProvider(PsiElement element) {
    super(element);
  }

  @Override
  protected boolean isReferenceAvailable() {
    var isIncludeElement = GitlabCIYamlUtils.isIncludeElement(element);
    var isYamlTextElement = YamlUtils.isYamlTextElement(element);
    return isIncludeElement && isYamlTextElement;
  }

  @Override
  protected Optional<PsiReference[]> getReferences() {
    var isNonLocalInclude = PsiUtils.isChild(element, NON_LOCAL_INCLUDE_KEYWORDS);
    if (isNonLocalInclude) {
      var includePathCacheKey = getIncludeCacheKey(element);
      var includePath = CIAidCacheService.getInstance().getIncludeCacheFilePathFromKey(includePathCacheKey);
      if (includePath != null) {
        return Optional.of(new PsiReference[]{ new IncludeFileReferenceResolver(element, includePath) });
      }
    } else {
      var filePattern = handleQuotedText(element.getText());
      return Optional.of(new PsiReference[]{ new IncludeFileReferenceResolver(element, filePattern) });
    }
    return Optional.of(PsiReference.EMPTY_ARRAY);
  }

  public static String getIncludeCacheKey(@NotNull PsiElement element) {
    var isChildOfFile = PsiUtils.isChild(element, List.of(FILE));
    if (isChildOfFile) {
      var blockMappingOptional = PsiUtils.findParentOfType(element, YAMLBlockMappingImpl.class);
      if (blockMappingOptional.isPresent()) {
        var blockMapping = blockMappingOptional.get();
        var keyValues = blockMapping.getKeyValues();
        String projectName = null;
        String filePath = null;
        String ref = null;
        for (YAMLKeyValue keyValue : keyValues) {
          var keyText = keyValue.getKeyText();
          switch (keyText) {
            case PROJECT -> projectName = handleQuotedText(keyValue.getValueText());
            case FILE -> filePath = handleQuotedText(element.getText());
            case REF -> ref = handleQuotedText(keyValue.getValueText());
          }
        }
        if (projectName != null && filePath != null) {
          return CIAidCacheUtils.getProjectFileCacheKey(projectName, filePath, ref);
        }
      }
    }
    return handleQuotedText(element.getText());
  }
}
