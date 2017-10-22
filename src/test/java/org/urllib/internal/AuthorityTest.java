package org.urllib.internal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;
import org.urllib.UrlException;

public class AuthorityTest {

  @Test public void removeUpToOneTrailingDotInHost_dns() {

    assertEquals(
        Authority.asciiDns("host", -1, Arrays.asList("host")),
        Authority.split("host"));

    assertEquals(
        Authority.asciiDns("host", -1, Arrays.asList("host")),
        Authority.split("host."));

    assertEquals(
        Authority.asciiDns("host.com", -1, Arrays.asList("host", "com")),
        Authority.split("host.com"));

    assertEquals(
        Authority.asciiDns("host.com", -1, Arrays.asList("host", "com")),
        Authority.split("host.com."));

  }

  @Ignore("IPv4 addresses not supported yet.")
  @Test public void removeUpToOneTrailingDotInHost_ipv4() {
    assertEquals(
        Authority.asciiDns("192.168.1.1", -1, Arrays.asList("192", "168", "1", "1")),
        Authority.split("192.168.1.1"));

    assertEquals(
        Authority.asciiDns("192.168.1.1", -1, Arrays.asList("192", "168", "1", "1")),
        Authority.split("192.168.1.1."));
  }

  @Test public void supportUnicodePeriods() {
    assertEquals("host.com", Authority.split("host。com").host().toString());
    assertEquals("host.com", Authority.split("host．com").host().toString());
    assertEquals("host.com", Authority.split("host｡com").host().toString());
  }

  @Test public void ipAddressesNotSupportedYet() {
    expectUnsupportedOperationException("8.8.8.8");
    expectUnsupportedOperationException("8.8.8.8:80");
    expectUnsupportedOperationException("user@host.com@8.8.8.8");
    expectUnsupportedOperationException("aa:bb::d");
    expectUnsupportedOperationException("[aa:bb:c]:9999");
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

  private void expectUnsupportedOperationException(String str) {
    try {
      Authority authority = Authority.split(str);
      fail("Expected UnsupportedOperationException; result was: " + authority);
    } catch (UnsupportedOperationException expected) {
    }
  }

  private void expectUrlException(String str, String msg) {
    try {
      Authority authority = Authority.split(str);
      fail("Expected UrlException; result was: " + authority);
    } catch (UrlException expected) {
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