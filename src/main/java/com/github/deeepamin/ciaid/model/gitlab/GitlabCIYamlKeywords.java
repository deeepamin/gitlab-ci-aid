package com.github.deeepamin.ciaid.model.gitlab;

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
  public static final String ARTIFACTS = "artifacts";
  public static final String OPTIONAL = "optional";
  public static final String LOCAL = "local";
  public static final String PROJECT = "project";
  public static final String REF = "ref";
  public static final String PIPELINE = "pipeline";
  public static final String PARALLEL = "parallel";
  public static final String MATRIX = "matrix";
  public static final String COMPONENT = "component";
  public static final String FILE = "file";
  public static final String REMOTE = "remote";
  public static final String TEMPLATE = "template";
  public static final String RULES = "rules";
  public static final String EXTENDS = "extends";
  public static final String SPEC = "spec";
  public static final String INPUTS = "inputs";
  public static final String TYPE = "type";
  public static final String DESCRIPTION = "description";
  public static final String STRING = "string";
  public static final String BOOLEAN = "boolean";
  public static final String NUMBER = "number";
  public static final String ARRAY = "array";

  public static final List<String> TOP_LEVEL_KEYWORDS = List.of(
          AFTER_SCRIPT,
          BEFORE_SCRIPT,
          SCRIPT,
          CACHE,
          DEFAULT,
          IMAGE,
          INCLUDE,
          PAGES,
          SERVICES,
          STAGES,
          VARIABLES,
          WORKFLOW,
          SPEC
  );

  public static final List<String> SCRIPT_KEYWORDS = List.of(SCRIPT, AFTER_SCRIPT, BEFORE_SCRIPT);

  public static final List<String> NEEDS_POSSIBLE_CHILD_KEYWORDS = List.of(ARTIFACTS, OPTIONAL, PROJECT, REF, PIPELINE, PARALLEL, MATRIX);
  public static final List<String> NON_LOCAL_INCLUDE_KEYWORDS = List.of(COMPONENT, PROJECT, FILE, REF, REMOTE, TEMPLATE, INPUTS, RULES);


  public static final String PRE_STAGE = ".pre";
  public static final String BUILD_STAGE = "build";
  public static final String TEST_STAGE = "test";
  public static final String DEPLOY_STAGE = "deploy";
  public static final String POST_STAGE = ".post";
  public static final List<String> DEFAULT_STAGES = List.of(PRE_STAGE, BUILD_STAGE, TEST_STAGE, DEPLOY_STAGE, POST_STAGE);
}
