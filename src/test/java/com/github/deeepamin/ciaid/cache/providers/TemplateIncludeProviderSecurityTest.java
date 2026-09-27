package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.cache.CIAidCacheService;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.intellij.ide.trustedProjects.TrustedProjects;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.mockito.MockedStatic;

import java.nio.file.Path;

import static org.mockito.Mockito.mockStatic;

public class TemplateIncludeProviderSecurityTest extends BaseIntegrationTest {
  public void testYamlProcessingIsSkippedForUntrustedProject() {
    VirtualFile yamlFile = myFixture.addFileToProject(".gitlab-ci.yml", "include:\n  - template: shell.bat\n");
    var projectService = CIAidProjectService.getInstance(getProject());

    try (MockedStatic<TrustedProjects> trustedProjects = mockStatic(TrustedProjects.class)) {
      trustedProjects.when(() -> TrustedProjects.isProjectTrusted(getProject())).thenReturn(false);

      projectService.readGitlabCIYamlData(yamlFile, false, true);

      assertFalse(projectService.getPluginData().containsKey(yamlFile));
    }
  }

  public void testRemoteIncludeIsNotReadForUntrustedProject() {
    var provider = new TrackingTemplateIncludeProvider(getProject());
    try (MockedStatic<TrustedProjects> trustedProjects = mockStatic(TrustedProjects.class)) {
      trustedProjects.when(() -> TrustedProjects.isProjectTrusted(getProject())).thenReturn(false);

      provider.readIncludeFile();

      assertNull(provider.cachedFile);
    }
  }

  public void testAbsoluteTemplatesPathIsRejected() {
    var settings = CIAidSettingsState.getInstance(getProject());
    settings.setGitlabTemplatesPath(Path.of(System.getProperty("java.io.tmpdir"), "attacker").toString());
    var provider = new TrackingTemplateIncludeProvider(getProject());

    provider.readRemoteIncludeFile();

    assertNull(provider.cachedFile);
  }

  public void testPathTraversalInTemplateNameIsRejected() {
    var settings = CIAidSettingsState.getInstance(getProject());
    settings.setGitlabTemplatesPath("lib/gitlab/ci/templates");
    var provider = new TrackingTemplateIncludeProvider(getProject(), "../../../../outside.bat");

    provider.readRemoteIncludeFile();

    assertNull(provider.cachedFile);
  }

  public void testRelativeTemplatePathStaysInsideCacheDirectory() {
    var settings = CIAidSettingsState.getInstance(getProject());
    settings.setGitlabTemplatesPath("lib/gitlab/ci/templates");
    var provider = new TrackingTemplateIncludeProvider(getProject());

    provider.readRemoteIncludeFile();

    assertNotNull(provider.cachedFile);
    assertTrue(provider.cachedFile.startsWith(
            Path.of(CIAidCacheService.getCiAidCacheDir().getPath(), "templates")
                    .toAbsolutePath().normalize()));
  }

  private static class TrackingTemplateIncludeProvider extends TemplateIncludeProvider {
    private Path cachedFile;

    private TrackingTemplateIncludeProvider(Project project) {
      this(project, "shell.bat");
    }

    private TrackingTemplateIncludeProvider(Project project, String filePath) {
      super(project, filePath);
    }

    @Override
    protected void validateAndCacheRemoteFile(String downloadUrl, String cacheKey, String cacheFilePath) {
      cachedFile = Path.of(cacheFilePath).toAbsolutePath().normalize();
    }
  }
}
