package com.github.deeepamin.ciaid.cache.providers;

import com.github.deeepamin.ciaid.BaseTest;

import java.util.Map;

import static com.github.deeepamin.ciaid.cache.providers.ComponentIncludeProvider.ComponentProjectNameVersion;

public class ComponentIncludeProviderTest extends BaseTest {
  private ComponentIncludeProvider componentIncludeProvider;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    componentIncludeProvider = new ComponentIncludeProvider(getProject(), GITLAB_CI_DEFAULT_YAML_FILE);
  }

  public void testGetComponentProjectNameAndVersion() {
    Map<String, ComponentProjectNameVersion> inputOutputs  = Map.of(
            "https://gitlab.com/my-org/security-components/secret-detection@1.0.0",
            new ComponentProjectNameVersion("my-org/security-components", "secret-detection", "1.0.0"),
            "$CI_SERVER_FQDN/my-org/security-components/secret-detection@1.0.0",
            new ComponentProjectNameVersion("my-org/security-components", "secret-detection", "1.0.0"),
            "https://gitlab.com/my-org/security-components/internal/secret-detection@~latest?ref=main",
            new ComponentProjectNameVersion("my-org/security-components/internal", "secret-detection", "~latest"),
            "http://gitlab.com/my-org/security-components/internal/secret-detection@~latest?ref=main",
            new ComponentProjectNameVersion("my-org/security-components/internal", "secret-detection", "~latest"),
            "$CI_SERVER_FQDN/my-org/security-components/internal/secrets/detection/secrets-detection@2",
            new ComponentProjectNameVersion("my-org/security-components/internal/secrets/detection", "secrets-detection", "2")
            );
    inputOutputs.forEach((input, expectedOutput) -> {
      var actualOutput = componentIncludeProvider.getComponentProjectNameAndVersion(input);
      assertNotNull(actualOutput);
      assertEquals(expectedOutput.project(), actualOutput.project());
      assertEquals(expectedOutput.component(), actualOutput.component());
      assertEquals(expectedOutput.version(), actualOutput.version());
    });
    }
}
