package org.urllib.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PercentDecoderTest {

  @Test public void noDecode() {
    assertEquals("", PercentDecoder.decode("", CodepointMatcher.NONE));
    assertEquals("str", PercentDecoder.decode("str", CodepointMatcher.NONE));
    assertEquals("%30", PercentDecoder.decode("%30", CodepointMatcher.NONE));
  }

  @Test public void decode() {
    assertEquals("0", PercentDecoder.decode("%30", CodepointMatcher.ALL));
    assertEquals("0a", PercentDecoder.decode("%30a", CodepointMatcher.ALL));
    assertEquals("0ab", PercentDecoder.decode("%30ab", CodepointMatcher.ALL));
    assertEquals("a0", PercentDecoder.decode("a%30", CodepointMatcher.ALL));
    assertEquals("a0a", PercentDecoder.decode("a%30a", CodepointMatcher.ALL));
    assertEquals("%a0a", PercentDecoder.decode("%a%30a", CodepointMatcher.ALL));
  }

}