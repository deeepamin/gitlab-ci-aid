package com.github.deeepamin.ciaid.utils;

import com.github.deeepamin.ciaid.BaseTest;

public class CIAidUtilsTest extends BaseTest {
  public void testHandleQuotedText() {
    assertEquals("test", CIAidUtils.handleQuotedText("\"test\""));
    assertEquals("\"test", CIAidUtils.handleQuotedText("\"test"));
    assertEquals("test\"", CIAidUtils.handleQuotedText("test\""));
    assertEquals("test", CIAidUtils.handleQuotedText("'test'"));
    assertEquals("'test", CIAidUtils.handleQuotedText("'test"));
    assertEquals("test'", CIAidUtils.handleQuotedText("test'"));
  }

  public void testContainsWildcard() {
    // Should detect wildcard
    assertTrue(CIAidUtils.containsWildcard("foo*bar"));
    assertTrue(CIAidUtils.containsWildcard("*.yml"));
    assertTrue(CIAidUtils.containsWildcard("/ci/*/test.yml"));
    assertTrue(CIAidUtils.containsWildcard("abc*"));
    assertTrue(CIAidUtils.containsWildcard("*"));

    // Should not detect wildcard
    assertFalse(CIAidUtils.containsWildcard("foobar"));
    assertFalse(CIAidUtils.containsWildcard("/ci/test.yml"));
    assertFalse(CIAidUtils.containsWildcard("") );
  }
}
