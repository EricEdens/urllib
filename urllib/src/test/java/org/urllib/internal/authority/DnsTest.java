package org.urllib.internal.authority;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class DnsTest {

  @Test public void dontAllowLeadingDots() {
    assertInvalid(".example.com");
  }

  @Test public void dontAllowEmptySegments() {
    assertInvalid("example..com");
  }

  @Test public void convertToLowerCase() {
    assertEquals(Dns.parse("example.com"), Dns.parse("EXAMPLE.com"));
  }

  private void assertInvalid(String host) {
    try {
      Dns.parse(host);
      fail("Expected IllegalArgumentException for: " + host);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("Invalid hostname"));
    }
  }
}