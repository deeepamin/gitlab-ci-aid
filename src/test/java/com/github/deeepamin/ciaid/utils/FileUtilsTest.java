package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.BaseTest;

import java.util.List;
import java.util.Map;

public class FileUtilsTest extends BaseTest {
  public void testGetFilePaths() {
    var scriptPathAndExpectedValues = Map.of("./test/test.sh", "./test/test.sh",
            "./test.sh", "./test.sh",
            "cd dir && ./test.sh", "./test.sh",
            "./test.sh $ARG", "./test.sh",
            "python /ci/test.py", "/ci/test.py",
            "python3 /ci/test.py", "/ci/test.py",
            "python3 ./test.py", "./test.py",
            "python3 ci/test.py", "ci/test.py",
            "pip install -r requirements.txt", "requirements.txt",
            "CUR_VERSION=$(cat ./version.txt)", "./version.txt");
    scriptPathAndExpectedValues.forEach((actual, expected) -> {
      var scriptPaths = FileUtils.getFilePathAndIndexes(actual);
      assertNotNull(scriptPaths);
      var actualScriptPath = scriptPaths.getFirst().path();
      assertEquals(expected, actualScriptPath);
    });
  }

  public void testMultipleGetFilePaths() {
    var multipleScriptPathAndExpectedValues = Map.of(
            "cp common/infra/test_report.py common/infra/reports.html .", List.of("common/infra/test_report.py", "common/infra/reports.html"),
            "python3 test_report.py > reports/index.html", List.of("test_report.py", "reports/index.html")
    );

    multipleScriptPathAndExpectedValues.forEach((actual, expected) -> {
      var scriptPaths = FileUtils.getFilePathAndIndexes(actual).stream().map(FileUtils.StringWithStartEndRange::path).toList();
      assertNotNull(scriptPaths);
      assertEquals(expected, scriptPaths);
    });
  }
}
