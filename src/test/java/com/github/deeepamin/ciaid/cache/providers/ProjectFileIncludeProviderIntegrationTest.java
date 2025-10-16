package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.cache.CIAidCacheUtils;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabConnectionUtils;

import java.io.File;
import java.nio.file.Files;

public class ProjectFileIncludeProviderIntegrationTest extends BaseIntegrationTest {
  private ProjectFileIncludeProvider provider;
  private static final String MOCK_YAML_CONTENT = "test:\n  script:\n    - echo 'test'";
  private static final String PROJECT_PATH = "my-group/my-project";
  private static final String FILE_PATH = "ci/build.yml";
  private static final String REF = "main";

  @Override
  public void setUp() throws Exception {
    super.setUp();

    tempCacheDir = Files.createTempDirectory("ciaid-test-cache").toFile();
    tempCacheDir.deleteOnExit();

    var settings = CIAidSettingsState.getInstance(getProject());
    settings.setCachingEnabled(true);
    settings.setGitLabAccessToken("test-token");

    provider = new ProjectFileIncludeProvider(getProject(), FILE_PATH, PROJECT_PATH, REF);
  }

  public void testGetCacheDirName() {
    var cacheDir = provider.getCacheDir();
    assertEquals("Cache directory should be 'projects'", "projects", cacheDir.getName());
  }

  public void testGetProjectPath() {
    String projectPath = provider.getProjectPath();
    assertEquals("Should return project path", PROJECT_PATH, projectPath);
  }

  public void testCachingDisabled_SkipsProcessing() {
    disableCaching();

    provider.readIncludeFile();

    var cacheDir = provider.getCacheDir();
    if (cacheDir.exists()) {
      File[] files = cacheDir.listFiles();
      assertTrue("Should not create cache files when caching is disabled",
              files == null || files.length == 0);
    }
  }

  public void testGetCacheFileDirectoryString() {
    var provider1 = new ProjectFileIncludeProvider(getProject(), "ci/templates/build.yml", "group/project", "main");
    var provider2 = new ProjectFileIncludeProvider(getProject(), "build.yml", "group/project", null);
    var provider3 = new ProjectFileIncludeProvider(getProject(), "a/b/c/test.yml", "org/repo", "dev");

    assertNotNull(provider1);
    assertNotNull(provider2);
    assertNotNull(provider3);

    assertEquals("projects", provider1.getCacheDir().getName());
    assertEquals("projects", provider2.getCacheDir().getName());
    assertEquals("projects", provider3.getCacheDir().getName());
  }

  public void testReadRemoteIncludeFile_HandlesInvalidInputs() {
    var provider1 = new ProjectFileIncludeProvider(getProject(), FILE_PATH, null, REF);
    provider1.readRemoteIncludeFile();

    var provider2 = new ProjectFileIncludeProvider(getProject(), "", PROJECT_PATH, REF);
    provider2.readRemoteIncludeFile();

    var provider3 = new ProjectFileIncludeProvider(getProject(), "script.sh", PROJECT_PATH, REF);
    provider3.readRemoteIncludeFile();
  }

  public void testCachedProjectFileResolvesCorrectPath() {
    try (var mockedUtils = org.mockito.Mockito.mockStatic(GitLabConnectionUtils.class)) {
      // Mock the download URL generation
      String expectedUrl = "https://gitlab.com/api/v4/projects/my-group%2Fmy-project/repository/files/ci%2Fbuild.yml/raw?ref=main";

      mockedUtils.when(() -> GitLabConnectionUtils.getRepositoryFileDownloadUrl(
              org.mockito.ArgumentMatchers.eq(getProject()),
              org.mockito.ArgumentMatchers.eq(PROJECT_PATH),
              org.mockito.ArgumentMatchers.eq(FILE_PATH),
              org.mockito.ArgumentMatchers.eq(REF)))
              .thenReturn(expectedUrl);

      // Mock the content download
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(
              org.mockito.ArgumentMatchers.eq(expectedUrl),
              org.mockito.ArgumentMatchers.anyString()))
              .thenReturn(MOCK_YAML_CONTENT);

      // Execute the read operation to cache the file
      provider.readRemoteIncludeFile();

      // Verify the cache service has stored the metadata
      var cacheService = CIAidCacheService.getInstance();

      // Use the same cache key generation method as the production code
      String cacheKey = CIAidCacheUtils.getProjectFileCacheKey(PROJECT_PATH, FILE_PATH, REF);
      String cachedFilePath = cacheService.getIncludeCacheFilePathFromKey(cacheKey);

      assertNotNull("Cache service should have stored the file path for the project file", cachedFilePath);

      // Verify the cached file path points to the correct location
      File cachedFile = new File(cachedFilePath);
      assertTrue("Cached file should exist at resolved path: " + cachedFilePath, cachedFile.exists());

      // Verify the file contains the expected content
      String cachedContent = Files.readString(cachedFile.toPath());
      assertEquals("Cached file content should match", MOCK_YAML_CONTENT, cachedContent);

      // Verify the cache service has stored file metadata (for expiry tracking)
      assertFalse("Cache should not be expired for newly cached file",
              cacheService.isCacheExpired(cachedFilePath));

      // Verify the cached file path contains the expected structure
      assertTrue("Cached file path should contain project name",
              cachedFilePath.contains("my-group_my-project"));
      assertTrue("Cached file path should contain ref",
              cachedFilePath.contains("main"));
      assertTrue("Cached file path should contain file path",
              cachedFilePath.contains("ci") && cachedFilePath.contains("build.yml"));
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }
}
