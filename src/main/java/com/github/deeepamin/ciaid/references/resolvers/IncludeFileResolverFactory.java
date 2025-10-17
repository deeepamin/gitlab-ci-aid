package com.github.deeepamin.ciaid.references.resolvers;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.cache.CIAidCacheUtils;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.PsiUtils;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;

import java.util.List;

import static com.github.deeepamin.ciaid.model.gitlab.GitlabCIYamlKeywords.*;
import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class IncludeFileResolverFactory {
  public static IncludeFileResolver getIncludeFileResolver(@NotNull PsiElement element) {
    var isNonLocalInclude = PsiUtils.isChild(element, NON_LOCAL_INCLUDE_KEYWORDS);
    if (isNonLocalInclude) {
      var includePathCacheKey = IncludeFileResolverFactory.getIncludeCacheKey(element);
      var includePath = CIAidCacheService.getInstance().getIncludeCacheFilePathFromKey(includePathCacheKey);
      if (includePath != null) {
        return new NonLocalIncludeFileResolver(element, includePath);
      }
    } else {
      var filePattern = handleQuotedText(element.getText());
      if (CIAidUtils.containsWildcardWithYmlExtension(filePattern)) {
        return new WildcardPatternIncludeFileResolver(element, filePattern);
      } else {
        return new LocalIncludeFileResolver(element, filePattern);
      }
    }
    return null;
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

