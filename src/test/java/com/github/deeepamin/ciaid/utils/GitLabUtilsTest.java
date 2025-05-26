package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.BaseTest;

import java.util.Map;

public class GitLabUtilsTest extends BaseTest {

  public void testGetComponentProjectNameAndVersion() {
    Map<String, GitLabUtils.ComponentProjectNameVersion> inputOutputs  = Map.of(
            "https://gitlab.com/my-org/security-components/secret-detection@1.0.0",
            new GitLabUtils.ComponentProjectNameVersion("my-org/security-components", "secret-detection", "1.0.0"),
            "$CI_SERVER_FQDN/my-org/security-components/secret-detection@1.0.0",
            new GitLabUtils.ComponentProjectNameVersion("my-org/security-components", "secret-detection", "1.0.0"),
            "https://gitlab.com/my-org/security-components/internal/secret-detection@~latest?ref=main",
            new GitLabUtils.ComponentProjectNameVersion("my-org/security-components/internal", "secret-detection", "~latest"),
            "http://gitlab.com/my-org/security-components/internal/secret-detection@~latest?ref=main",
            new GitLabUtils.ComponentProjectNameVersion("my-org/security-components/internal", "secret-detection", "~latest"),
            "$CI_SERVER_FQDN/my-org/security-components/internal/secrets/detection/secrets-detection@2",
            new GitLabUtils.ComponentProjectNameVersion("my-org/security-components/internal/secrets/detection", "secrets-detection", "2")
            );
    inputOutputs.forEach((input, expectedOutput) -> {
      var actualOutput = GitLabUtils.getComponentProjectNameAndVersion(input);
      assertNotNull(actualOutput);
      assertEquals(expectedOutput.project(), actualOutput.project());
      assertEquals(expectedOutput.component(), actualOutput.component());
      assertEquals(expectedOutput.version(), actualOutput.version());
    });
    }
}
