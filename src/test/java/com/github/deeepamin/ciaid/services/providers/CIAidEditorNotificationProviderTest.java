package com.github.deeepamin.ciaid.services.providers;

import com.github.deeepamin.ciaid.BaseTest;
import com.github.deeepamin.ciaid.services.CIAidProjectService;
import com.github.deeepamin.ciaid.settings.CIAidSettingsState;
import com.intellij.ui.EditorNotificationPanel;

import static com.github.deeepamin.ciaid.services.CIAidProjectService.GITLAB_CI_YAML_POTENTIAL_KEY;
import static com.github.deeepamin.ciaid.services.CIAidProjectService.GITLAB_CI_YAML_USER_MARKED_KEY;

public class CIAidEditorNotificationProviderTest extends BaseTest {
  private CIAidEditorNotificationProvider myProvider;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myProvider = new CIAidEditorNotificationProvider();
  }

  @Override
  public void tearDown() throws Exception {
    var settings = CIAidSettingsState.getInstance(getProject());
    settings.setEditorNotificationDisabled(false);
    super.tearDown();
  }

  public void testNotificationShownForPotentialGitlabCIYaml() {
    myFixture.configureByText("my-pipeline.yml", """
        stages:
          - build
        build-job:
          stage: build
          script:
            - echo hello
        """);
    var file = myFixture.getFile().getVirtualFile();

    // First call sets potential key
    var notificationData = myProvider.collectNotificationData(getProject(), file);
    assertNotNull(notificationData);

    // Simulate that the potential key was set to true
    file.putUserData(GITLAB_CI_YAML_POTENTIAL_KEY, true);

    // Second call should return a function that produces a panel
    notificationData = myProvider.collectNotificationData(getProject(), file);
    assertNotNull(notificationData);
    // The panel is produced regardless of the fileEditor argument when potential key is already set
    var panel = notificationData.apply(null);
    assertNotNull(panel);
    assertInstanceOf(panel, EditorNotificationPanel.class);
  }

  public void testNoNotificationForNonYamlFile() {
    myFixture.configureByText("Main.java", "public class Main {}");
    var file = myFixture.getFile().getVirtualFile();

    var notificationData = myProvider.collectNotificationData(getProject(), file);
    // For non-yaml files the function is returned but produces no panel
    if (notificationData != null) {
      assertNull(notificationData.apply(null));
    }
  }

  public void testNoNotificationWhenDisabled() {
    CIAidSettingsState.getInstance(getProject()).setEditorNotificationDisabled(true);

    myFixture.configureByText("my-pipeline.yml", """
        stages:
          - build
        """);
    var file = myFixture.getFile().getVirtualFile();

    var notificationData = myProvider.collectNotificationData(getProject(), file);
    assertNull(notificationData);
  }

  public void testNoNotificationForAlreadyReadFile() {
    myFixture.configureByText(GITLAB_CI_DEFAULT_YAML_FILE, """
        stages:
          - build
        build-job:
          stage: build
          script:
            - echo hello
        """);
    var file = myFixture.getFile().getVirtualFile();
    var projectService = CIAidProjectService.getInstance(getProject());
    projectService.readGitlabCIYamlData(file, false, false);

    var notificationData = myProvider.collectNotificationData(getProject(), file);
    assertNull(notificationData);
  }

  public void testNoNotificationForUserMarkedFile() {
    myFixture.configureByText("my-pipeline.yml", """
        stages:
          - build
        """);
    var file = myFixture.getFile().getVirtualFile();
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, true);

    var notificationData = myProvider.collectNotificationData(getProject(), file);
    assertNull(notificationData);

    // cleanup
    file.putUserData(GITLAB_CI_YAML_USER_MARKED_KEY, null);
  }

  public void testNoNotificationForNonGitlabYaml() {
    // A YAML file with "kind" key (e.g. Kubernetes) should not show notification
    myFixture.configureByText("deployment.yml", """
        kind: Deployment
        apiVersion: apps/v1
        """);
    var file = myFixture.getFile().getVirtualFile();

    var notificationData = myProvider.collectNotificationData(getProject(), file);
    assertNotNull(notificationData);
    // The function should return null since it's not a GitLab CI YAML
    var panel = notificationData.apply(null);
    assertNull(panel);
  }

  public void testNoNotificationForYamlWithoutGitlabKeywords() {
    myFixture.configureByText("random.yml", """
        foo: bar
        baz: qux
        """);
    var file = myFixture.getFile().getVirtualFile();

    var notificationData = myProvider.collectNotificationData(getProject(), file);
    assertNotNull(notificationData);
    var panel = notificationData.apply(null);
    assertNull(panel);
  }
}
