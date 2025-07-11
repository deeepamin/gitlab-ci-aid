package com.github.deeepamin.ciaid.model.gitlab.include;

public enum IncludeFileType {
  LOCAL,
  REMOTE,
  PROJECT,
  TEMPLATE,
  COMPONENT;

  public static IncludeFileType fromString(String type) {
    if (type == null || type.isBlank()) {
      return LOCAL;
    }
    return switch (type.toLowerCase()) {
      case "remote" -> REMOTE;
      case "project" -> PROJECT;
      case "template" -> TEMPLATE;
      case "component" -> COMPONENT;
      default -> LOCAL;
    };
  }
}
