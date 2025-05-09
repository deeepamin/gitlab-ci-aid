package com.github.deeepamin.ciaid.services.providers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.model.InputDocumentationTarget;
import com.intellij.platform.backend.documentation.DocumentationData;
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

@SuppressWarnings("UnstableApiUsage")
public class CIAidDocumentationTargetProviderTest extends BaseTest {
  public void testInputElementShowsDocumentation() {
    myFixture.configureByText(".gitlab-ci.yml", """
            spec:
              inputs:
                stageInput:
                  description: The stage the job will run in.
                  type: string
                  default: build
            ---
            build-dev:
              needs:
                - $[[ inputs.stageInput<caret> ]]
          """);
    PsiFile file = myFixture.getFile();
    int offset = myFixture.getCaretOffset();
    PsiElement leafPsiElement = myFixture.getFile().findElementAt(offset);
    assertNotNull(leafPsiElement);
    PsiElement element = leafPsiElement.getParent();

    var providers = PsiDocumentationTargetProvider.EP_NAME.getExtensionList();
    assertNotNull(providers);
    assertEquals(1, providers.size());
    var documentationTargetProvider = providers.getFirst();
    assertTrue(documentationTargetProvider instanceof CIAidDocumentationTargetProvider);
    var ciAidDocumentationTargetProvider = (CIAidDocumentationTargetProvider) documentationTargetProvider;

    var documentationTarget = ciAidDocumentationTargetProvider.documentationTarget(file, element);
    assertNotNull(documentationTarget);
    assertTrue(documentationTarget instanceof InputDocumentationTarget);
    var inputDocumentationTarget = (InputDocumentationTarget) documentationTarget;

    var presentation = inputDocumentationTarget.computePresentation();
    assertNotNull(presentation);
    assertEquals("stageInput Documentation", presentation.getPresentableText());
    assertEquals("/src/.gitlab-ci.yml", presentation.getLocationText());
    assertNotNull(presentation.getLocationIcon());

    var documentationResult = inputDocumentationTarget.computeDocumentation();
    var expectedHtml = "<div style=\"font-family: JetBrains Mono; font-weight: bold; margin-bottom: 5px;\">stageInput: string</div>" +
                       "<hr/>" +
                       "<div style=\"margin-top: 5px; margin-bottom: 5px;\">The stage the job will run in.</div>" +
                       "<span style=\"color: gray; margin-bottom: 8px;\">default: build</span>";
    assertNotNull(documentationResult);
    assertTrue(documentationResult instanceof DocumentationData);
    var documentationData = (DocumentationData) documentationResult;
    assertEquals(expectedHtml, documentationData.getHtml());
  }
}
