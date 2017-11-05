package org.urllib.internal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class AuthorityTest {

  @Test public void ipv4() {
    assertEquals("1.1.1.1",
        Authority.split("1.1.1.1").host().name());
    assertEquals("192.168.1.1",
        Authority.split("user:password@192.168.1.1").host().name());
  }

  @Test public void ipv4_removeUpToOneTrailingDotInHost() {
    assertEquals("192.168.1.1",
        Authority.split("192.168.1.1.").host().name());
  }

  @Test public void supportUnicodePeriods() {
    assertEquals("host.com", Authority.split("host。com").host().toString());
    assertEquals("host.com", Authority.split("host．com").host().toString());
    assertEquals("host.com", Authority.split("host｡com").host().toString());
  }

  @Test public void failWhenHostIsEmpty() {
    expectUrlException("", "Host cannot be empty");
    expectUrlException(":", "Host cannot be empty");
    expectUrlException("user@host.com@", "Host cannot be empty");
  }

  @Test public void removeUserInfo() {
    assertEquals("host.com", Authority.split("user@host.com").host().toString());
    assertEquals("host.com", Authority.split("user@host.com@host.com").host().toString());
    assertEquals("host.com", Authority.split("user@host.com@host.com:80").host().toString());
  }

  @Test public void rejectInvalidPorts() {
    expectInvalidPortException("host.com:ab");
    expectInvalidPortException("host.com:65536");
    expectInvalidPortException("host.com:0");
    expectInvalidPortException("host.com:-1");
  }

  @Test public void trimLeadingZeroesInPort() {
    assertEquals(1, Authority.split("h:000000001").port());
    assertEquals(10, Authority.split("h:010").port());
    assertEquals(65535, Authority.split("h:00065535").port());
  }

  @Test public void defaultPortIsMinusOne() {
    assertEquals(-1, Authority.split("h").port());
    assertEquals(-1, Authority.split("h:").port());
  }

  private void expectUrlException(String str, String msg) {
    try {
      Authority authority = Authority.split(str);
      fail("Expected UrlException; result was: " + authority);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString(msg));
    }
  }

  private void expectInvalidPortException(String str) {
    try {
      Authority authority = Authority.split(str);
      fail("Expected IllegalArgumentException; result was: " + authority);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("Invalid port in authority."));
    }
  }
}