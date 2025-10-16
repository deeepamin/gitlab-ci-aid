package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.github.deeepamin.ciaid.utils.GitLabConnectionUtils;
import org.mockito.MockedStatic;

import java.io.File;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

public class RemoteUrlIncludeProviderIntegrationTest extends BaseIntegrationTest {
  private RemoteUrlIncludeProvider provider;
  private static final String MOCK_YAML_CONTENT = "stages:\n  - build\n  - test";
  private static final String REMOTE_URL = "https://example.com/ci/templates/common.yml";

  @Override
  public void setUp() throws Exception {
    super.setUp();

    tempCacheDir = Files.createTempDirectory("ciaid-test-remote-cache").toFile();
    tempCacheDir.deleteOnExit();

    var settings = CIAidSettingsState.getInstance(getProject());
    settings.setCachingEnabled(true);

    provider = new RemoteUrlIncludeProvider(getProject(), REMOTE_URL);
  }

  public void testReadRemoteIncludeFile_DownloadsFromUrl() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(eq(REMOTE_URL), eq(null)))
              .thenReturn(MOCK_YAML_CONTENT);

      provider.readRemoteIncludeFile();

      var cacheDir = provider.getCacheDir();
      assertTrue("Cache directory should exist", cacheDir.exists());

      File[] cachedFiles = cacheDir.listFiles((dir, name) -> name.endsWith(".yml"));
      assertNotNull("Should have cached files", cachedFiles);
      assertEquals("Should have exactly one cached file", 1, cachedFiles.length);

      String cachedContent = Files.readString(cachedFiles[0].toPath());
      assertEquals("Cached content should match", MOCK_YAML_CONTENT, cachedContent);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  public void testGetAccessToken_ReturnsNull() {
    String token = provider.getAccessToken();
    assertNull("Access token should be null for remote URLs", token);
  }

  public void testGetProjectPath_ReturnsNull() {
    String projectPath = provider.getProjectPath();
    assertNull("Project path should be null for remote URLs", projectPath);
  }

  public void testGetCacheDirName() {
    var cacheDir = provider.getCacheDir();
    assertEquals("Cache directory name should be 'remote'", "remote", cacheDir.getName());
  }

  public void testReadRemoteIncludeFile_HandlesDownloadFailure() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(eq(REMOTE_URL), eq(null)))
              .thenReturn(null);

      provider.readRemoteIncludeFile();

      var cacheDir = provider.getCacheDir();
      if (cacheDir.exists()) {
        File[] cachedFiles = cacheDir.listFiles((dir, name) -> name.endsWith(".yml"));
        assertTrue("Should not have cached files on failure",
                   cachedFiles == null || cachedFiles.length == 0);
      }
    } catch (Exception e) {
      fail("Should handle download failure gracefully: " + e.getMessage());
    }
  }

  public void testReadRemoteIncludeFile_MultipleUrls() {
    try (MockedStatic<GitLabConnectionUtils> mockedUtils = mockStatic(GitLabConnectionUtils.class)) {
      String url1 = "https://example.com/template1.yml";
      String url2 = "https://example.com/template2.yml";
      String content1 = "template: 1";
      String content2 = "template: 2";

      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(eq(url1), eq(null)))
              .thenReturn(content1);
      mockedUtils.when(() -> GitLabConnectionUtils.downloadContent(eq(url2), eq(null)))
              .thenReturn(content2);

      var provider1 = new RemoteUrlIncludeProvider(getProject(), url1);
      var provider2 = new RemoteUrlIncludeProvider(getProject(), url2);

      provider1.readRemoteIncludeFile();
      provider2.readRemoteIncludeFile();

      var cacheDir = provider1.getCacheDir();
      if (cacheDir.exists()) {
        File[] cachedFiles = cacheDir.listFiles((dir, name) -> name.endsWith(".yml"));
        assertTrue("Should have multiple cached files",
                   cachedFiles != null && cachedFiles.length >= 2);
      }
    } catch (Exception e) {
      fail("Test failed: " + e.getMessage());
    }
  }
}
