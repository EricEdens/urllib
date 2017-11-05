package org.urllib.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringsTest {

  @Test public void codepoints() {
    // Two java characters collapse into a single codepoint
    assertArrayEquals(new int[]{65536}, Strings.codePoints("\uD800\uDC00"));
    assertArrayEquals(new int[]{9731}, Strings.codePoints("☃"));
  }

  @Test public void isNullOrEmpty() {
    assertTrue(Strings.isNullOrEmpty(null));
    assertTrue(Strings.isNullOrEmpty(""));
    assertFalse(Strings.isNullOrEmpty("a"));
  }

  @Test public void santizeWhitespace() {
    assertEquals("", Strings.sanitizeWhitespace("   "));
    assertEquals("a", Strings.sanitizeWhitespace(" a  "));
    assertEquals("ab", Strings.sanitizeWhitespace(" a\nb  "));
    assertEquals("ab", Strings.sanitizeWhitespace(" a\n  b  "));
    assertEquals("ab", Strings.sanitizeWhitespace("a\n  b"));
    assertEquals("ab  c", Strings.sanitizeWhitespace("a\n  b  c"));
    assertEquals("ab  c", Strings.sanitizeWhitespace("a\n  b  c"));
    assertEquals("a", Strings.sanitizeWhitespace("a\n  "));
  }

}