package org.urllib;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;
import org.junit.Test;

public class UrlTest {

  @Test public void onlyPath() {
    Url url = Url.http("host").create();
    assertEquals("http://host/", url.toString());

    url = Url.http("host").path("").create();
    assertEquals("http://host/", url.toString());

    url = Url.http("host").path("/").create();
    assertEquals("http://host/", url.toString());
  }

  @Test public void onlyQuery() {
    Url url = Url.http("host").query("key", "val").create();
    assertEquals("http://host/?key=val", url.toString());

    url = Url.http("host").query("key", "").create();
    assertEquals("http://host/?key", url.toString());
  }

  @Test public void onlyFragment() {
    Url url = Url.http("host").fragment("fragment").create();
    assertEquals("http://host/#fragment", url.toString());

    url = Url.http("host").fragment("").create();
    assertEquals("http://host/", url.toString());
  }

  @Test public void builderFullUrl() {
    assertEquals("http://localhost:8080/files/pdf/report.pdf?user=8123#Reports",
        Url.http("localhost:8080")
            .path("/files/pdf/", "report.pdf")
            .query(Collections.singletonMap("user", "8123"))
            .fragment("Reports")
            .toString());

    assertEquals("https://www.google.co.jp/search?q=%E5%AD%90%E7%8C%AB",
        Url.https("www.google.co.jp")
            .path("/search")
            .query(Collections.singletonMap("q", "子猫"))
            .toString());

    assertEquals("http://wikipedia.org/wiki/Moli%C3%A8re",
        Url.http("wikipedia.org")
            .path("/wiki", "Molière")
            .toString());

    assertEquals("https://twitter.com/search?q=%F0%9F%A4%A8",
        Url.https("twitter.com")
            .path("/search")
            .query(Collections.singletonMap("q", "\uD83E\uDD28"))
            .toString());

    assertEquals("https://www.wolframalpha.com/input/?i=%CF%80%C2%B2",
        Url.https("www.wolframalpha.com")
            .path("input/")
            .query(Collections.singletonMap("i", "π²"))
            .toString());
  }

  @Test public void scheme() {
    Url url = Url.http("host").create();
    assertEquals(Scheme.HTTP, url.scheme());

    url = Url.https("host").create();
    assertEquals(Scheme.HTTPS, url.scheme());
  }

  @Test public void host() throws Exception {
    Url url = Url.http("host").create();
    assertEquals("host", url.host().toString());

    url = Url.http("10.10.0.1:9000").create();
    assertEquals("10.10.0.1", url.host().toString());

    url = Url.http("2001:0db8:0000:0000:0000:8a2e:0370:7334").create();
    assertEquals("2001:db8::8a2e:370:7334", url.host().toString());
    assertEquals("http://[2001:db8::8a2e:370:7334]/", url.toString());

    url = Url.http("[2001:db8::8a2e:370:7334]").create();
    assertEquals("2001:db8::8a2e:370:7334", url.host().toString());
    assertEquals("http://[2001:db8::8a2e:370:7334]/", url.toString());

    url = Url.http("[::A]:9000").create();
    assertEquals("::a", url.host().toString());
    assertEquals("http://[::a]:9000/", url.toString());
    assertEquals(9000, url.port());
  }

  @Test public void host_dontAllowInvalidIpv4() {
    assertInvalidHost("10.10.0", "Invalid hostname");
    assertInvalidHost("1.1.1", "Invalid hostname");
    assertInvalidHost("0xa.0xb.0xc.0xd", "Invalid hostname");
    assertInvalidHost("3294823", "Invalid hostname");
    assertInvalidHost("1.1.1.1.1", "Invalid hostname");
    assertInvalidHost("-1:-1:-1:-1", "Invalid hostname");
  }

  @Test public void host_dontAllowInvalidIpv6() {
    assertInvalidHost(":::", "Invalid hostname");
    assertInvalidHost("1:2:3:4:5:6:7:8:9", "Invalid hostname");
    assertInvalidHost("a::z:80", "Invalid hostname");
  }

  @Test public void host_dontAllowInvalidDns() {
    assertInvalidHost("host .com", "Invalid hostname");
    assertInvalidHost("host_name.com", "Invalid hostname");
  }

  @Test public void host_removesUserInfo() {
    Url url = Url.http("user:password@host.com").create();
    assertEquals("host.com", url.host().toString());

    url = Url.http("user@domain.com:password@host.com").create();
    assertEquals("host.com", url.host().toString());
  }

  @Test public void host_convertCharactersToLowerCase() {
    assertEquals("abcd", Url.http("ABCD").create().host().toString());
    assertEquals("σ", Url.http("Σ").create().host().toString());
  }

  @Test public void host_idnEncodedAsPunycode() {
    Url url = Url.http("bücher").create();
    assertEquals("http://xn--bcher-kva/", url.toString());
    assertEquals("xn--bcher-kva", url.host().name());
    assertEquals("bücher", url.host().toString());
  }

  @Test public void host_builderTakesPunycodeOrUnicode() {
    Url unicode = Url.http("bücher").create();
    Url punycode = Url.http("xn--bcher-kva").create();
    assertEquals(unicode, punycode);
    assertEquals(unicode.hashCode(), punycode.hashCode());
  }

  @Test public void port() {
    Url url = Url.http("host").create();
    assertEquals(80, url.port());

    url = Url.http("host:443").create();
    assertEquals(443, url.port());

    url = Url.http("host").port(8080).create();
    assertEquals(8080, url.port());

    url = Url.https("host").create();
    assertEquals(443, url.port());

    url = Url.https("host:80").create();
    assertEquals(80, url.port());
  }

  @Test public void fragment() {
    Url url = Url.http("host")
        .fragment("\uD83D\uDC36")
        .create();
    assertEquals("\uD83D\uDC36", url.fragment().toString());
  }

  @Test public void allowPortWithHost() {
    assertEquals("http://localhost:8080/", Url.http("localhost:8080").toString());
    assertEquals("http://localhost/", Url.http("localhost:80").toString());
  }

  private void assertInvalidHost(String host, String msg) {
    try {
      Url.http(host);
      fail("Expected IllegalArgumentException for: " + host);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString(msg));
    }
  }
}