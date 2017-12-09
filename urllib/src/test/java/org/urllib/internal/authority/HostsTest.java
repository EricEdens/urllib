package org.urllib.internal.authority;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class HostsTest {

  @Test public void allowTrailingDot() {
    assertEquals(Hosts.parse("example.com"), Hosts.parse("example.com."));
    assertEquals(Hosts.parse("example"), Hosts.parse("example."));
    assertEquals(Hosts.parse("1.1.1.1"), Hosts.parse("1.1.1.1."));
  }

  @Test public void dontAllowEmptySegments() {
    assertInvalidHost("");
    assertInvalidHost(" ");
    assertInvalidHost(".");
    assertInvalidHost("%2e");
    assertInvalidHost("..");
    assertInvalidHost("host..com");
    assertInvalidHost("1.1..1.1");

    assertInvalidHost(":");
    assertInvalidHost("[]");
    assertInvalidHost("[:]");
  }

  @Test public void convertToLowerCase() {
    assertEquals(Hosts.parse("example.com"), Hosts.parse("Example.com"));
    assertEquals(Hosts.parse("ökonom.de"), Hosts.parse("Ökonom.de"));
    assertEquals(Hosts.parse("ли.ru"), Hosts.parse("Ли.ru"));
  }

  static void assertInvalidHost(String host) {
    try {
      Hosts.parse(host);
      fail("Expected IllegalArgumentException for: " + host);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("Invalid hostname"));
    }
  }

}