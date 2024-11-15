package com.github.deeepamin.gitlabciaid.utils;

import com.github.deeepamin.gitlabciaid.BaseTest;
import java.util.Map;

public class FileUtilsTest extends BaseTest {

  public void testGetShOrPyScript() {
    var scriptPathAndExpectedValues = Map.of("./test/test.sh", "./test/test.sh",
            "./test.sh", "./test.sh",
            "cd dir && ./test.sh", "./test.sh",
            "./test.sh $ARG", "./test.sh",
            "python /ci/test.py", "/ci/test.py",
            "python3 /ci/test.py", "/ci/test.py",
            "python3 ./test.py", "./test.py");
    scriptPathAndExpectedValues.forEach((actual, expected) -> {
      var scriptPathIndex = FileUtils.getShOrPyScript(actual);
      assertNotNull(scriptPathIndex);
      var actualScriptPath = scriptPathIndex.path();
      assertEquals(expected, actualScriptPath);
    });
  }
}
