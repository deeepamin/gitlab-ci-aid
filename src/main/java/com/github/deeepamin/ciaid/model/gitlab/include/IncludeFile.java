package com.github.deeepamin.ciaid.model.gitlab.include;

import static com.github.deeepamin.ciaid.utils.CIAidUtils.handleQuotedText;

public class IncludeFile {
  protected String path;
  protected IncludeFileType fileType;

  public IncludeFileType getFileType() {
    return fileType;
  }

  public void setFileType(IncludeFileType fileType) {
    this.fileType = fileType;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = handleQuotedText(path);
  }
}
