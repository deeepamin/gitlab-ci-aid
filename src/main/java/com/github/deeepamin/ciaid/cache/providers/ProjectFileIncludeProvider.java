package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.utils.GitLabUtils;
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
  public void readRemoteIncludeFile() {
    if (projectPath == null || filePath == null || projectPath.isBlank() || filePath.isBlank()) {
      LOG.debug("Project name or file path in gitlab project is null or empty");
      return;
    }

    var fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
    if (!YamlUtils.hasYamlExtension(fileName)) {
      LOG.debug("Not a YAML file: " + fileName);
      return;
    }

    var filePathWithoutFileName = filePath.substring(0, filePath.lastIndexOf("/"));
    var cacheFileDirectoryString = projectPath.replaceAll("/", "_") +
            File.separator +
            (ref != null && !ref.isBlank() ? ref + File.separator : "") +
            filePathWithoutFileName.replaceAll("/", File.separator);
    var cacheFilePath = Paths.get(getCacheDir().getAbsolutePath()).resolve(cacheFileDirectoryString).resolve(fileName);

    var downloadUrl = GitLabUtils.getRepositoryFileDownloadUrl(project, projectPath, filePath, ref);
    var cacheKey = getProjectFileCacheKey(projectPath, filePath, ref);
    validateAndCacheRemoteFile(downloadUrl, cacheKey, cacheFilePath.toString());
  }
}
