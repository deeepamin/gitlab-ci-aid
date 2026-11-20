package com.github.deeepamin.ciaid.services;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.cache.providers.LocalIncludeProvider;
import com.github.deeepamin.ciaid.cache.providers.ProjectFileIncludeProvider;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeFile;
import com.github.deeepamin.ciaid.model.gitlab.include.IncludeFileType;

public class CIAidYamlDataProviderTest extends BaseTest {
  public void testLocalIncludeFromCachedProjectFileUsesSameProjectAndRef() {
    var include = new IncludeFile();
    include.setFileType(IncludeFileType.LOCAL);
    include.setPath("/.common.gitlab-ci.yml");
    var context = new CIAidCacheService.ProjectFileIncludeContext("group/templates", "main");

    var provider = new CIAidYamlDataProvider(getProject()).createIncludeProvider(include, false, context);

    assertTrue(provider instanceof ProjectFileIncludeProvider);
    assertEquals("group/templates", ((ProjectFileIncludeProvider) provider).getProjectPath());
  }

  public void testLocalIncludeFromConsumerUsesLocalProvider() {
    var include = new IncludeFile();
    include.setFileType(IncludeFileType.LOCAL);
    include.setPath("pipeline.yml");

    var provider = new CIAidYamlDataProvider(getProject()).createIncludeProvider(include, false, null);

    assertTrue(provider instanceof LocalIncludeProvider);
  }
}
