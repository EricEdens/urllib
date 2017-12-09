package org.urllib.internal.authority;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class Ip4Test {

  @Test public void removeTrailingPeriod() {
    assertEquals("255.255.255.255", Hosts.parse("255.255.255.255.").name());
  }

  @Test public void percentDecodeBeforeParsing() {
    assertEquals("1.1.1.1", Hosts.parse("%31.%31.%31.%31").name());
    assertEquals("1.1.1.1", Hosts.parse("1%2e1%2e1%2e1").name());
  }

  @Test public void disambiguateUnicodeFirst() {
    assertEquals("1.1.1.1", Hosts.parse("１。１。１。１").name());
  }

  @Test public void ipv4() {
    assertEquals("1.1.1.1", Hosts.parse("1.1.1.1").name());
    assertEquals("0.0.0.1", Hosts.parse("0.0.0.1").name());
    assertEquals("0.0.0.0", Hosts.parse("0.0.0.0").name());
    assertEquals("255.255.255.255", Hosts.parse("255.255.255.255").name());
  }

  @Test public void wrongNumberSegments() {
    assertInvalid("1");
    assertInvalid("1.1");
    assertInvalid("1.1.1");
    assertInvalid("1.1.1.1.1");
  }

  @Test public void outOfRange() {
    assertInvalid("-1.1.1.1");
    assertInvalid("1.1.1.256");
    assertInvalid("1.1.1.1000");
  }

  @Test public void notAllNumeric() {
    assertInvalid("A.1.1.1");
    assertInvalid("1.1.A.1");
  }

  @Test public void dontAllowLeadingZero() {
    assertInvalid("4.89.8.05");
    assertInvalid("01.1.1.1");
  }


  private static void assertInvalid(String host) {
    try {
      Hosts.parse(host);
      fail("Expected IllegalArgumentException for: " + host);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("Invalid hostname"));
    }
  }
}