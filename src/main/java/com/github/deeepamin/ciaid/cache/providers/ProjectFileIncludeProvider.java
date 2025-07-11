package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.utils.GitLabConnectionUtils;
import com.github.deeepamin.ciaid.utils.YamlUtils;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.nio.file.Paths;

import static com.github.deeepamin.ciaid.cache.CIAidCacheUtils.getProjectFileCacheKey;

public class ProjectFileIncludeProvider extends AbstractRemoteIncludeProvider {
  private final String projectPath;
  private final String ref;

  public ProjectFileIncludeProvider(Project project, String filePath, String projectPath, String ref) {
    super(project, filePath);
    this.projectPath = projectPath;
    this.ref = ref;
  }

  @Override
  protected String getCacheDirName() {
    return "projects";
  }

  @Override
  public String getProjectPath() {
    return projectPath;
  }

  @Override
  public void readRemoteIncludeFile() {
    if (projectPath == null || filePath == null || projectPath.isBlank() || filePath.isBlank()) {
      LOG.debug("Project name or file path in gitlab project is null or empty");
      return;
    }

    var fileName = filePath.contains("/") ? filePath.substring(filePath.lastIndexOf("/") + 1) : filePath;
    if (!YamlUtils.hasYamlExtension(fileName)) {
      LOG.debug("Not a YAML file: " + fileName);
      return;
    }

    var filePathWithoutFileName = filePath.contains("/") ? filePath.substring(0, filePath.lastIndexOf("/")) : filePath;
    var cacheFileDirectoryString = projectPath.contains("/") ? projectPath.replaceAll("/", "_") : projectPath +
            File.separator +
            (ref != null && !ref.isBlank() ? ref + File.separator : "") +
            (filePathWithoutFileName.contains("/") ? filePathWithoutFileName.replaceAll("/", File.separator) : filePathWithoutFileName);
    var cacheFilePath = Paths.get(getCacheDir().getAbsolutePath()).resolve(cacheFileDirectoryString).resolve(fileName);

    var downloadUrl = GitLabConnectionUtils.getRepositoryFileDownloadUrl(project, projectPath, filePath, ref);
    var cacheKey = getProjectFileCacheKey(projectPath, filePath, ref);
    validateAndCacheRemoteFile(downloadUrl, cacheKey, cacheFilePath.toString());
  }
}
