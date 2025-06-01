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
}
