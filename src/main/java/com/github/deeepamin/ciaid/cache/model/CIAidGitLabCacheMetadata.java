package com.github.deeepamin.ciaid.cache.model;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CIAidGitLabCacheMetadata implements Serializable {
  // public to serialize the object to XML
  public final Map<String, String> metaData = new ConcurrentHashMap<>();
  private static final String PATH = "path";
  private static final String EXPIRY_TIME_KEY = "expiryTime";
  private static final String PROJECT_PATH_KEY = "projectPath";
  private static final String REF_KEY = "ref";

  public String getPath() {
    return metaData.getOrDefault(PATH, "");
  }

  public CIAidGitLabCacheMetadata path(String path) {
    metaData.put(PATH, path);
    return this;
  }

  public Long getExpiryTime() {
    return Long.parseLong(metaData.getOrDefault(EXPIRY_TIME_KEY, "-1"));
  }

  public CIAidGitLabCacheMetadata expiryTime(Long expiryTime) {
    metaData.put(EXPIRY_TIME_KEY, String.valueOf(expiryTime));
    return this;
  }

  public String getProjectPath() {
    return metaData.get(PROJECT_PATH_KEY);
  }

  public String getRef() {
    return metaData.get(REF_KEY);
  }

  public void projectFileContext(String projectPath, String ref) {
    metaData.put(PROJECT_PATH_KEY, projectPath);
    if (ref != null) {
      metaData.put(REF_KEY, ref);
    }
  }
}
