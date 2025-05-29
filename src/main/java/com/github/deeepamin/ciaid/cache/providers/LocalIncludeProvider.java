package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.services.CIAidProjectService;
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
    FileUtils.getVirtualFile(sanitizedYamlPath, project)
            .ifPresent(includeVirtualFile -> ciAidProjectService.readGitlabCIYamlData(project, includeVirtualFile, userMarked));
  }
}
