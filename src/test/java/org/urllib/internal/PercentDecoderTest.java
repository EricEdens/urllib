package org.urllib.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class PercentDecoderTest {

  @Test public void allAscii() {
    StringBuilder expected = new StringBuilder();
    StringBuilder encoded = new StringBuilder();

    for (char c = 0; c < 0x80; c++) {
      expected.append(c);
      encoded.append(percentEncode(c));
    }
    assertEquals(expected.toString(), PercentDecoder.decodeAll(encoded.toString()));
  }

  @Test public void allAsciiUnreserved() {
    StringBuilder expected = new StringBuilder();
    StringBuilder encoded = new StringBuilder();

    for (char c = 0; c < 0x80; c++) {
      String percent = percentEncode(c);
      if (".-_~".indexOf(c) > -1 || CodepointMatcher.ALPHANUMERIC.matches(c)) {
        expected.append(c);
      } else {
        expected.append(percent);
      }
      encoded.append(percent);
    }
    assertEquals(expected.toString(), PercentDecoder.decodeUnreserved(encoded.toString()));
  }

  @Test public void decodingSafelyHandlesMalformedPercents() {
    // Using assertSame to check that we don't allocate a new string
    // when decoding isn't possible.
    assertSame("%zz", PercentDecoder.decodeAll("%zz"));
    assertSame("%3", PercentDecoder.decodeAll("%3"));
    assertSame("%3z", PercentDecoder.decodeAll("%3z"));
    assertSame("%", PercentDecoder.decodeAll("%"));
    assertSame("%%2", PercentDecoder.decodeAll("%%2"));
    assertSame("%2%", PercentDecoder.decodeAll("%2%"));
  }

  private String percentEncode(int c) {
    return String.format("%%%02X", c);
  }
}