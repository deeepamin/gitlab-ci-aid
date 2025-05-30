package com.github.deeepamin.ciaid.cache.model;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CIAidGitLabCacheMetadata implements Serializable {
  private final Map<String, String> metaData = new ConcurrentHashMap<>();
  private static final String PATH = "path";
  private static final String EXPIRY_TIME_KEY = "expiryTime";

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
}
