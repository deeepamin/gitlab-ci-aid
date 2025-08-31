package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.utils.CIAidUtils;
import com.github.deeepamin.ciaid.utils.FileUtils;
import com.intellij.openapi.project.Project;

public class LocalIncludeProvider extends AbstractIncludeProvider {
  private final boolean userMarked;

  public LocalIncludeProvider(Project project, String filePath, boolean userMarked) {
    super(project, filePath);
    this.userMarked = userMarked;
  }

  @Override
  public void readIncludeFile() {
    var sanitizedYamlPath = FileUtils.sanitizeFilePath(filePath);
    var ciAidProjectService = CIAidProjectService.getInstance(project);
    if (CIAidUtils.containsWildcardWithYmlExtension(sanitizedYamlPath)) {
      var matches = FileUtils.findVirtualFilesByGlob(sanitizedYamlPath, project);
      for (var includeVirtualFile : matches) {
        ciAidProjectService.readGitlabCIYamlData(includeVirtualFile, userMarked, false);
      }
    } else {
      FileUtils.getVirtualFile(sanitizedYamlPath, project)
              .ifPresent(includeVirtualFile -> ciAidProjectService.readGitlabCIYamlData(includeVirtualFile, userMarked, false));
    }
  }

}
