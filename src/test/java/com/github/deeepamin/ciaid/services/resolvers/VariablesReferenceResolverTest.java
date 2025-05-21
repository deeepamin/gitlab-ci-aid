package com.github.deeepamin.ciaid.services.resolvers;

import com.github.deeepamin.ciaid.BaseTest;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;

public class VariablesReferenceResolverTest extends BaseTest {
  private static final String TEST_DIR_PATH = getOsAgnosticPath("/ReferenceResolverTest/Variables");

  public void testSameFileOnlyDollar() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            variables:
              IMAGE_TAG: test-tag
              DOCKER_TLS_CERTDIR: /certs
            
            build:
              stage: build
              image: $IMAGE_TAG
              script:
                - echo "Building with image: $IMAGE<caret>_TAG"
            """);
    var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    assertNotNull(reference);
    assertTrue(reference instanceof PsiMultiReference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValue);
    assertEquals("IMAGE_TAG", ((YAMLKeyValue) resolve).getKeyText());
  }

  public void testSameFileDollarWithCurlies() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
            variables:
              IMAGE_TAG: test-tag
              DOCKER_TLS_CERTDIR: /certs
            
            build:
              stage: build
              image: $IMAGE_TAG
              script:
                - echo "Building with image: ${DOCKER_<caret>TLS_CERTDIR}"
            """);
    var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    assertNotNull(reference);
    assertTrue(reference instanceof PsiMultiReference);
    var resolve = reference.resolve();
    assertNotNull(resolve);
    assertTrue(resolve instanceof YAMLKeyValue);
    assertEquals("DOCKER_TLS_CERTDIR", ((YAMLKeyValue) resolve).getKeyText());
  }

  public void testAnotherFile() {
    var testDir = getTestDirectoryName();
    var reference = myFixture.getReferenceAtCaretPosition(TEST_DIR_PATH + File.separator + testDir + File.separator + GITLAB_CI_DEFAULT_YAML_FILE, TEST_DIR_PATH + File.separator + testDir + PIPELINE_YML);
    assertNotNull(reference);
    assertTrue(reference instanceof PsiMultiReference);
  }
}
