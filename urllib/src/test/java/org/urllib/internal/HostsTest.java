package org.urllib.internal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.urllib.internal.Hosts;

public class HostsTest {

  @Test public void dontAllowLeadingDots() {
    assertInvalid(".com");
    assertInvalid(".1.1.1.1");
  }

  @Test public void dontAllowEmptySegments() {
    assertInvalid("host..com");
    assertInvalid("1.1..1.1");
  }

  private void assertInvalid(String host) {
    try {
      Hosts.parse(host);
      fail("Expected IllegalArgumentException for: " + host);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("Invalid hostname"));
    }
  }

}