package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabConnectionUtils;
import org.mockito.MockedStatic;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static com.github.deeepamin.ciaid.cache.providers.ComponentIncludeProvider.ComponentProjectNameVersion;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

public class ComponentIncludeProviderIntegrationTest extends BaseIntegrationTest {
  private ComponentIncludeProvider provider;
  private static final String MOCK_COMPONENT_YAML = "spec:\n  inputs:\n    stage:\n      default: test";
  private static final String COMPONENT_PATH = "https://gitlab.com/my-org/security-components/secret-detection@1.0.0";
  private static final String MOCK_TAGS_JSON = "[{\"name\":\"v1.0.0\"},{\"name\":\"v1.0.1\"},{\"name\":\"v2.0.0\"}]";

  @Override
  public void setUp() throws Exception {
    super.setUp();

    tempCacheDir = Files.createTempDirectory("ciaid-test-component-cache").toFile();
    tempCacheDir.deleteOnExit();

    var settings = CIAidSettingsState.getInstance(getProject());
    settings.setCachingEnabled(true);
    settings.setGitLabAccessToken("test-token");

    provider = new ComponentIncludeProvider(getProject(), COMPONENT_PATH);
  }

  public void testGetComponentProjectNameAndVersion_VariousFormats() {
    Map<String, ComponentProjectNameVersion> testCases = Map.of(
            "https://gitlab.com/my-org/security-components/secret-detection@1.0.0",
            new ComponentProjectNameVersion("my-org/security-components", "secret-detection", "1.0.0"),

            "$CI_SERVER_FQDN/my-org/security-components/secret-detection@1.0.0",
            new ComponentProjectNameVersion("my-org/security-components", "secret-detection", "1.0.0"),

            "https://gitlab.com/my-org/security-components/internal/secret-detection@~latest?ref=main",
            new ComponentProjectNameVersion("my-org/security-components/internal", "secret-detection", "~latest"),

            "http://gitlab.com/my-org/security-components/internal/secret-detection@~latest?ref=main",
            new ComponentProjectNameVersion("my-org/security-components/internal", "secret-detection", "~latest"),

            "$CI_SERVER_FQDN/my-org/security-components/internal/secrets/detection/secrets-detection@2",
            new ComponentProjectNameVersion("my-org/security-components/internal/secrets/detection", "secrets-detection", "2")
    );

    testCases.forEach((input, expectedOutput) -> {
      var actualOutput = provider.getComponentProjectNameAndVersion(input);
      assertNotNull("Should parse component path: " + input, actualOutput);
      assertEquals("Project should match for: " + input, expectedOutput.project(), actualOutput.project());
      assertEquals("Component should match for: " + input, expectedOutput.component(), actualOutput.component());
      assertEquals("Version should match for: " + input, expectedOutput.version(), actualOutput.version());
    });
  }

  public void testReadRemoteIncludeFile_DownloadsComponent() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      // Mock the download URL generation for template.yml
      String expectedUrl = "https://gitlab.com/api/v4/projects/my-org%2Fsecurity-components/repository/files/templates%2Fsecret-detection%2Ftemplate.yml/raw?ref=1.0.0";

      mockedUtils.when(() -> GitLabConnectionUtils.getRepositoryFileDownloadUrl(
              eq(getProject()),
              eq("my-org/security-components"),
              eq("templates/secret-detection/template.yml"),
              eq("1.0.0")))
              .thenReturn(expectedUrl);

      // Mock the content download
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(eq(expectedUrl), anyString()))
              .thenReturn(MOCK_COMPONENT_YAML);

      // Execute the read operation
      provider.readRemoteIncludeFile();

      // Verify the file was cached
      var cacheDir = provider.getCacheDir();
      assertTrue("Cache directory should exist", cacheDir.exists());
      assertEquals("Cache dir should be 'components'", "components", cacheDir.getName());

      // The file should be in: components/my-org_security-components/1.0.0/templates/secret-detection/template.yml
      File expectedCachedFile = new File(cacheDir, "my-org_security-components/1.0.0/templates/secret-detection/template.yml");
      assertTrue("Cached file should exist at: " + expectedCachedFile.getPath(), expectedCachedFile.exists());

      // Verify content
      String cachedContent = Files.readString(expectedCachedFile.toPath());
      assertEquals("Cached content should match", MOCK_COMPONENT_YAML, cachedContent);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  public void testResolveComponentVersion_LatestVersion() {
    var componentWithLatest = new ComponentIncludeProvider(
            getProject(),
            "https://gitlab.com/my-org/project/component@~latest");

    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      // Mock HTTP response for tags API
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(anyString(), anyString()))
              .thenReturn(MOCK_TAGS_JSON);

      String resolvedVersion = componentWithLatest.resolveComponentVersion(
              getProject(), "~latest", "test-token");

      // Should return the highest non-prerelease version
      assertEquals("Should resolve to highest version", "2.0.0", resolvedVersion);
    } catch (Exception e) {
      fail("Version resolution failed: " + e.getMessage());
    }
  }

  public void testResolveComponentVersion_PartialVersion() {
    var componentWithPartial = new ComponentIncludeProvider(
            getProject(),
            "https://gitlab.com/my-org/project/component@1");

    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      String tagsJson = "[{\"name\":\"v1.0.0\"},{\"name\":\"v1.0.1\"},{\"name\":\"v1.1.0\"},{\"name\":\"v2.0.0\"}]";

      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(anyString(), anyString()))
              .thenReturn(tagsJson);

      String resolvedVersion = componentWithPartial.resolveComponentVersion(
              getProject(), "1", "test-token");

      // Should return the highest 1.x.x version
      assertEquals("Should resolve to highest 1.x version", "1.1.0", resolvedVersion);
    } catch (Exception e) {
      fail("Partial version resolution failed: " + e.getMessage());
    }
  }

  public void testResolveComponentVersion_DirectSHA() {
    String sha = "abc123def456abc123def456abc123def456abc1";
    String resolvedVersion = provider.resolveComponentVersion(getProject(), sha, "test-token");

    assertEquals("SHA should be returned as-is", sha, resolvedVersion);
  }

  public void testResolveComponentVersion_ExactMatch() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(anyString(), anyString()))
              .thenReturn(MOCK_TAGS_JSON);

      String resolvedVersion = provider.resolveComponentVersion(
              getProject(), "1.0.0", "test-token");

      assertEquals("Exact version should be returned", "1.0.0", resolvedVersion);
    }
  }

  public void testReadRemoteIncludeFile_TriesBothPaths() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      // Mock both possible download URLs
      String templateUrl = "https://gitlab.com/api/v4/projects/my-org%2Fsecurity-components/repository/files/templates%2Fsecret-detection%2Ftemplate.yml/raw?ref=1.0.0";
      String componentUrl = "https://gitlab.com/api/v4/projects/my-org%2Fsecurity-components/repository/files/templates%2Fsecret-detection.yml/raw?ref=1.0.0";

      mockedUtils.when(() -> GitLabConnectionUtils.getRepositoryFileDownloadUrl(
              any(), anyString(), contains("template.yml"), anyString()))
              .thenReturn(templateUrl);

      mockedUtils.when(() -> GitLabConnectionUtils.getRepositoryFileDownloadUrl(
              any(), anyString(), contains(".yml"), anyString()))
              .thenReturn(componentUrl);

      // First URL fails, second succeeds
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(eq(templateUrl), anyString()))
              .thenReturn(null);
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(eq(componentUrl), anyString()))
              .thenReturn(MOCK_COMPONENT_YAML);

      provider.readRemoteIncludeFile();

      // Should have tried both URLs
      mockedUtils.verify(() -> GitLabConnectionUtils.getRepositoryFileDownloadUrl(
              any(), anyString(), anyString(), anyString()),
              org.mockito.Mockito.atLeast(2));
    } catch (Exception e) {
      fail("Test failed: " + e.getMessage());
    }
  }

  public void testGetComponentProjectNameAndVersion_InvalidPath() {
    var invalidProvider = new ComponentIncludeProvider(getProject(), "invalid-component-path");
    var result = invalidProvider.getComponentProjectNameAndVersion("invalid-component-path");

    assertNull("Should return null for invalid path", result);
  }

  public void testGetCacheDirName() {
    var cacheDir = provider.getCacheDir();
    assertEquals("Cache directory should be 'components'", "components", cacheDir.getName());
  }

  public void testGetProjectPath() {
    String projectPath = provider.getProjectPath();
    assertEquals("Should return project path", "my-org/security-components", projectPath);
  }

  public void testResolveComponentVersion_ApiFailure() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      // Simulate API failure
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(anyString(), anyString()))
              .thenReturn(null);

      String resolvedVersion = provider.resolveComponentVersion(
              getProject(), "~latest", "test-token");

      // Should return the original version reference on failure
      assertEquals("Should return original version on API failure", "~latest", resolvedVersion);
    }
  }

  public void testResolveComponentVersion_SkipsPreReleases() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      String tagsWithPreRelease = "[{\"name\":\"v1.0.0\"},{\"name\":\"v1.0.1-rc1\"},{\"name\":\"v1.1.0-beta\"},{\"name\":\"v2.0.0\"}]";

      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(anyString(), anyString()))
              .thenReturn(tagsWithPreRelease);

      String resolvedVersion = provider.resolveComponentVersion(
              getProject(), "~latest", "test-token");

      // Should skip pre-release versions and return 2.0.0
      assertEquals("Should skip pre-release versions", "2.0.0", resolvedVersion);
    }
  }

  public void testCachedComponentResolvesCorrectPath() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      // Mock the download URL generation
      String expectedUrl = "https://gitlab.com/api/v4/projects/my-org%2Fsecurity-components/repository/files/templates%2Fsecret-detection%2Ftemplate.yml/raw?ref=1.0.0";

      mockedUtils.when(() -> GitLabConnectionUtils.getRepositoryFileDownloadUrl(
              eq(getProject()),
              eq("my-org/security-components"),
              eq("templates/secret-detection/template.yml"),
              eq("1.0.0")))
              .thenReturn(expectedUrl);

      // Mock the content download
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(eq(expectedUrl), anyString()))
              .thenReturn(MOCK_COMPONENT_YAML);

      // Execute the read operation to cache the file
      provider.readRemoteIncludeFile();

      // Verify the cache service has stored the metadata
      var cacheService = CIAidCacheService.getInstance();

      // The cache key should be the original component path
      String cachedFilePath = cacheService.getIncludeCacheFilePathFromKey(COMPONENT_PATH);

      assertNotNull("Cache service should have stored the file path for the component", cachedFilePath);

      // Verify the cached file path points to the correct location
      File cachedFile = new File(cachedFilePath);
      assertTrue("Cached file should exist at resolved path: " + cachedFilePath, cachedFile.exists());

      // Verify the file contains the expected content
      String cachedContent = Files.readString(cachedFile.toPath());
      assertEquals("Cached file content should match", MOCK_COMPONENT_YAML, cachedContent);

      // Verify the cache service has stored file metadata (for expiry tracking)
      assertFalse("Cache should not be expired for newly cached file",
              cacheService.isCacheExpired(cachedFilePath));

      // Verify the cached file path contains the expected structure
      assertTrue("Cached file path should contain component name",
              cachedFilePath.contains("my-org_security-components"));
      assertTrue("Cached file path should contain version",
              cachedFilePath.contains("1.0.0"));
      assertTrue("Cached file path should contain template path",
              cachedFilePath.contains("templates"));
      assertTrue("Cached file path should contain component file name",
              cachedFilePath.contains("secret-detection"));
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }
}
