package com.github.deeepamin.gitlabciaid.model;

import java.util.List;

public class GitlabCIYamlKeywords {
  public static final String DEFAULT = "default";
  public static final String INCLUDE = "include";
  public static final String STAGES = "stages";
  public static final String VARIABLES = "variables";
  public static final String WORKFLOW = "workflow";
  public static final String AFTER_SCRIPT = "after_script";
  public static final String BEFORE_SCRIPT = "before_script";
  public static final String CACHE = "cache";
  public static final String IMAGE = "image";
  public static final String PAGES = "pages";
  public static final String SCRIPT = "script";
  public static final String SERVICES = "services";
  public static final String STAGE = "stage";
  public static final String NEEDS = "needs";

  public static final List<String> TOP_LEVEL_KEYWORDS = List.of(
          AFTER_SCRIPT,
          BEFORE_SCRIPT,
          CACHE,
          DEFAULT,
          IMAGE,
          INCLUDE,
          PAGES,
          SERVICES,
          STAGES,
          VARIABLES,
          WORKFLOW
  );

  public static final List<String> SCRIPT_KEYWORDS = List.of(SCRIPT, AFTER_SCRIPT, BEFORE_SCRIPT);
}
