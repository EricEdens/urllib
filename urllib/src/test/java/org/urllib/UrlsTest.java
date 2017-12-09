package org.urllib;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.junit.Test;
import org.urllib.Query.KeyValue;
import org.urllib.internal.Scheme;

public class UrlsTest {

  @Test public void emptyPathIsAlwaysForwardSlash() {
    Url expected = Urls.http("host").path("/").create();
    assertEquals(expected, Urls.http("host").create());
    assertEquals(expected, Urls.http("host").path("").create());
    assertEquals(expected, Urls.http("host").path("\\").create());
  }

  @Test public void scheme() {
    Url url = Urls.http("host").create();
    assertEquals(Scheme.HTTP.name(), url.scheme());

    url = Urls.https("host").create();
    assertEquals(Scheme.HTTPS.name(), url.scheme());
  }

  @Test public void host() throws Exception {
    Url url = Urls.http("host").create();
    assertEquals("host", url.host().display());

    url = Urls.http("10.10.0.1:9000").create();
    assertEquals("10.10.0.1", url.host().display());

    url = Urls.http("2001:0db8:0000:0000:0000:8a2e:0370:7334").create();
    assertEquals("2001:db8::8a2e:370:7334", url.host().display());

    url = Urls.http("[2001:db8::8a2e:370:7334]").create();
    assertEquals("2001:db8::8a2e:370:7334", url.host().display());

    url = Urls.http("[::A]:9000").create();
    assertEquals("::a", url.host().display());
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
    Url url = Urls.http("user:password@host.com").create();
    assertEquals("host.com", url.host().display());

    url = Urls.http("user@domain.com:password@host.com").create();
    assertEquals("host.com", url.host().display());
  }

  @Test public void host_convertCharactersToLowerCase() {
    assertEquals("abcd", Urls.http("ABCD").create().host().display());
    assertEquals("σ", Urls.http("Σ").create().host().display());
  }

  @Test public void host_idnEncodedAsPunycode() {
    Url url = Urls.http("bücher").create();
    assertEquals("xn--bcher-kva", url.host().name());
    assertEquals("bücher", url.host().display());
  }

  @Test public void host_builderTakesPunycodeOrUnicode() {
    Url unicode = Urls.http("bücher").create();
    Url punycode = Urls.http("xn--bcher-kva").create();
    assertEquals(unicode, punycode);
    assertEquals(unicode.hashCode(), punycode.hashCode());
  }

  @Test public void port() {
    Url url = Urls.http("host").create();
    assertEquals(80, url.port());

    url = Urls.http("host:443").create();
    assertEquals(443, url.port());

    url = Urls.http("host").port(8080).create();
    assertEquals(8080, url.port());

    url = Urls.https("host").create();
    assertEquals(443, url.port());

    url = Urls.https("host:80").create();
    assertEquals(80, url.port());
  }

  @Test public void fragment() {
    Url url = Urls.http("host")
        .fragment("\uD83D\uDC36")
        .create();
    assertEquals("\uD83D\uDC36", url.fragment());
  }

  @Test public void uriInteropAllCodepoints() {
    for (char point = 0; point < 0x100; point++) {
      String input = "" + point;
      Url url = Urls.http("host.com")
          .path("/" + input)
          .query(input, input)
          .fragment(input)
          .create();

      assertEquals(url.toString(), url.uri().toString());
    }
  }

  @Test public void uriInteropSpaceAndPlus() {
    Url url = Urls.parse("http://site.com/c++?q=%2B+-");
    URI uri = url.uri();
    assertEquals("/c++", uri.getPath());
    assertEquals("q=+ -", uri.getQuery());
  }

  @Test public void uriInteropUnicode() {
    Url url = Urls.parse("http://❄.com/❄?q=❄#❄");
    URI uri = url.uri();
    assertEquals("xn--tdi.com", uri.getHost());
    assertEquals("/❄", uri.getPath());
    assertEquals("q=❄", uri.getQuery());
    assertEquals("❄", uri.getFragment());
  }

  @Test public void uriInteropHash() {
    Url url = Urls.http("host.com")
        .path("/c#")
        .query("q", "#!")
        .fragment("#fragment#")
        .create();
    URI uri = url.uri();
    assertEquals("/c#", uri.getPath());
    assertEquals("q=#!", uri.getQuery());
    assertEquals("#fragment#", uri.getFragment());
  }

  @Test public void uriInteropIpv6() {
    Url url = Urls.http("[ff::00]")
        .create();
    URI uri = url.uri();
    assertEquals("[ff::]", uri.getHost());
  }

  @Test public void allowPortWithHost() {
    assertEquals(8080, Urls.http("localhost:8080").create().port());
    assertEquals(80, Urls.https("localhost:80").create().port());
  }

  @Test public void trimWhitespaceBeforeParsing() {
    assertEquals(Urls.http("example.com").create(), Urls.parse("  http://\nexample.\n  com  "));
  }

  @Test public void parseRequiresSchemeAndHost() {
    String msg = "must have a scheme and host";
    assertInvalidParse("host.com", msg);
    assertInvalidParse("//host.com", msg);
    assertInvalidParse("../path", msg);
    assertInvalidParse("path/info.pdf", msg);
    assertInvalidParse("#fragment", msg);
  }

  @Test public void parseSchemeMustBeHttpOrHttps() {
    String msg = "http or https";
    assertInvalidParse("mysql://host.com", msg);
    assertInvalidParse("ws://host.com", msg);
    assertInvalidParse("jdbc://host.com", msg);
    assertInvalidParse("ldap://host.com", msg);
  }

  @Test public void parseSchemeIsCaseInsensitive() {
    assertEquals(Scheme.HTTP.name(), Urls.parse("HTTP://host.com").scheme());
    assertEquals(Scheme.HTTPS.name(), Urls.parse("HtTPs://host.com").scheme());
  }

  @Test public void parsePrefersPortFromInputString() {
    assertEquals(443, Urls.parse("https://host.com").port());
    assertEquals(80, Urls.parse("http://host.com").port());

    assertEquals(9000, Urls.parse("http://host.com:9000").port());
    assertEquals(443, Urls.parse("http://host.com:443").port());
    assertEquals(80, Urls.parse("https://host.com:80").port());

    assertEquals(8080, Urls.parse("http://[a::443]:8080").port());
    assertEquals(8080, Urls.parse("http://1.1.1.1:8080").port());
  }

  @Test public void parseValidatesPort() {
    String msg = "Invalid port";
    assertInvalidParse("http://host.com:-1", msg);
    assertInvalidParse("http://host.com:0x01", msg);
    assertInvalidParse("http://host.com:10000000", msg);
  }

  @Test public void parseValidatesHost() {
    assertInvalidParse("http:////", "missing host");
    assertInvalidParse("http://?", "missing host");
    assertInvalidParse("http://#", "missing host");
    assertInvalidParse("http://host\u2000.com", "Invalid host");
    assertInvalidParse("http://!@[", "Invalid host");
    assertInvalidParse("http://[a:1:b]", "Invalid host");
    assertInvalidParse("http://[1:2:3:4:5:6:7:8:9]", "Invalid host");
    assertInvalidParse("http://192.168", "Invalid host");
    assertInvalidParse("http://1.1.1.1.1", "Invalid host");

    // Don't allow leading zeroes in ipv4 since it's unclear whether the encoding
    // is decimal or octal.
    assertInvalidParse("http://01.01.01.01", "Invalid host");
  }

  @Test public void parseConvertsDnsHostToLowerCase() {
    assertEquals("host.com", Urls.parse("http://HOST.com").host().name());
    assertEquals("host.com", Urls.parse("http://HoSt.COM").host().name());
  }

  @Test public void parseCompressesIpv6() {
    assertEquals("[a::1]", Urls.parse("http://[a:0:0:0:0:0:0:1]").host().name());
    assertEquals("[ff::]", Urls.parse("http://[FF::0:0]").host().name());
    assertEquals("[::a:b:0:0:0]", Urls.parse("http://[0:0:0:a:b:00:000:0]").host().name());
  }

  @Test public void parseAlwaysReturnsUrlWithPath() {
    Path expected = Path.empty();
    assertEquals(expected, Urls.parse("http://host.com").path());
    assertEquals(expected, Urls.parse("http://host.com/").path());
    assertEquals(expected, Urls.parse("http://host.com?query").path());
    assertEquals(expected, Urls.parse("http://host.com#fragment").path());
  }

  @Test public void parseRemovesDotSegmentsInPath() {
    assertEquals(Path.of("a/b/"), Urls.parse("http://host.com/a/b/").path());
    assertEquals(Path.of("a/b/"), Urls.parse("http://host.com/a/b/.").path());
    assertEquals(Path.of("a/b/"), Urls.parse("http://host.com/a/b/c/..").path());
    assertEquals(Path.of("a/b/"), Urls.parse("http://host.com/a/b/c/../.").path());
    assertEquals(Path.of("a/"), Urls.parse("http://host.com/a/b/c/../..").path());
    assertEquals(Path.of("a/b/file.html"), Urls.parse("http://host.com/a/b/c/../file.html").path());

    assertEquals(Path.of("a/b/"), Urls.parse("http://host.com/a/b/%2e").path());
    assertEquals(Path.of("a/b/"), Urls.parse("http://host.com/a/b/c/%2E%2e").path());
  }

  @Test public void parseRemovesEmptyQueryValues() {
    Query expected = Query.of(Arrays.asList(KeyValue.create("k", "")));
    assertEquals(expected, Urls.parse("http://host.com?k=").query());
    assertEquals(expected, Urls.parse("http://host.com?k").query());
  }

  @Test public void parseRetainsDuplicateKeysInQuery() {
    Query expected = Query.of(
        Arrays.asList(KeyValue.create("k", "a"), KeyValue.create("k", "b")));
    assertEquals(expected, Urls.parse("http://host.com?k=a&k=b").query());
  }

  @Test public void parseRemovesPercentEncoding() {
    Url decoded = Urls.parse("http://host.com/docs/résumé.html?q=\uD83D\uDC3C#\uD83D\uDE03");
    Url encoded = Urls.parse("http://host.com/docs/r%C3%A9sum%C3%A9.html?q=%F0%9F%90%BC#%F0%9F%98%83");
    assertEquals(decoded, encoded);
    assertEquals(decoded, encoded);
  }

  @Test public void parseHandlesSlashesInBothDirections() {
    assertEquals(Urls.parse("http://host.com/a/b/"), Urls.parse("http:\\\\host.com\\a\\b\\"));
  }

  private void assertInvalidParse(String url, String msg) {
    try {
      Urls.parse(url);
      fail("Expected IllegalArgumentException for: " + url);
    } catch (IllegalArgumentException expected) {
      if (!expected.getMessage().contains(msg)) {
        throw expected;
      }
    }
  }

  private void assertInvalidHost(String host, String msg) {
    try {
      Urls.http(host);
      fail("Expected IllegalArgumentException for: " + host);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString(msg));
    }
  }

  @Test public void minimalEscapeTrimsWhitespace() {
    String expected = "http://host";
    assertEquals(expected, Urls.escape(" http://host "));
    assertEquals(expected, Urls.escape("   http://host "));
    assertEquals(expected, Urls.escape(" http://host\n"));
    assertEquals(expected, Urls.escape("\thttp://host\n"));
    assertEquals(expected, Urls.escape("\fhttp://host\n"));
    assertEquals(expected, Urls.escape("\fhttp://host\n\r"));
  }

  @Test public void createURI() {
    assertEquals("http://host/path/", Urls.createURI("http:\\\\host\\path\\").toString());
    assertEquals("http://host/path/?q=%5C#%5C", Urls.createURI("http:\\\\host\\path\\?q=\\#\\").toString());
    assertEquals("http://test.org/res?signature=a+b=&init=a%20a", Urls.createURI("http://test.org/res?signature=a+b=&init=a a").toString());
    assertEquals("http://host/path;/?q=;%7C", Urls.createURI("http://host/path;/?q=;|").toString());
    assertEquals("https://en.wikipedia.org/wiki/A*", Urls.createURI("https://en.wikipedia.org/wiki/A*").toString());
    assertEquals("https://en.wikipedia.org/wiki/C++", Urls.createURI("https://en.wikipedia.org/wiki/C++").toString());
    assertEquals("https://en.wikipedia.org/wiki/%E2%9D%84", Urls.createURI("https://en.wikipedia.org/wiki/❄").toString());
    assertEquals("http://host/%2e", Urls.createURI("http://host/%2e").toString());
    assertEquals("http://host/%25zz", Urls.createURI("http://host/%zz").toString());
    assertEquals("http://[fa::dd]", Urls.createURI("http://FA::0:dd").toString());
    assertEquals("http://host.com:90", Urls.createURI("http://user:pass@host.com:90").toString());
  }

  @Test public void createURIHost() {
    String expected = "http://xn--qei";
    String input = "http://❤";
    assertEquals(expected, Urls.createURI(input).toString());
    assertEquals("http://host.com:90", Urls.createURI("http://user:pass@host.com:90").toString());
    assertEquals("http://host.com", Urls.createURI("http://HOST.com.").toString());
    assertEquals("http://host.com", Urls.createURI("http://HOST.com:").toString());
    assertEquals("http://host.com/", Urls.createURI("http://HOST.com:/").toString());
    assertEquals("http://192.168.1.1", Urls.createURI("http://192.168.1.1").toString());
    assertEquals("http://192.com", Urls.createURI("http://192.com").toString());
    assertEquals("http://192.com", Urls.createURI("http://192%2ecom").toString());
    assertEquals("http://[fa::dd]", Urls.createURI("http://FA::0:dd").toString());
  }

  @Test public void createURIBackSlashes() {
    assertEquals("http://host/path/?q=%5C#%5C",
        Urls.createURI("http:\\\\host\\path\\?q=\\#\\").toString());
  }

  @Test public void createURIPort() {
    assertEquals("http://host:80", Urls.createURI("http://host:80").toString());
  }

  @Test public void createURIPath() {
    assertEquals("http://host/%2e", Urls.createURI("http://host/%2e").toString());
    assertEquals("http://host/%25zz", Urls.createURI("http://host/%zz").toString());
  }

  @Test public void minimalEscapeRemovesLineBreaks() {
    String expected = "http://host";
    assertEquals(expected, Urls.escape("http://\nhost"));
    assertEquals(expected, Urls.escape("http://\n\rhost"));
    assertEquals(expected, Urls.escape("http://\rhost"));
    assertEquals(expected, Urls.escape("http://\r     host"));
    assertEquals(expected, Urls.escape("http://\n\thost"));
  }

  @Test public void minimalEncodeToLowerCase() {
    assertEquals("http://host", Urls.escape("HTTP://host"));
  }

  @Test public void minimalEncodeHostname() {
    assertEquals("http://xn--qei", Urls.escape("http://❤"));
    assertEquals("http://host.com:9000", Urls.escape("http://user:password@host.com:9000"));
    assertEquals("http://host.com", Urls.escape("http://HOST.com."));
    assertEquals("http://192.168.1.1", Urls.escape("http://192.168.1.1"));
    assertEquals("http://192.com", Urls.escape("http://192.com"));
    assertEquals("http://192.com", Urls.escape("http://192%2ecom"));
    assertEquals("http://[fa::dd]", Urls.escape("http://FA::0:dd"));
  }

  @Test public void minimalEncodeChecksAuthority() {
    try {
      Urls.escape("http://\\\\]/path");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("Invalid hostname:"));
    }
  }

  @Test public void minimalEncodeFixColonSlashSlash() {
    String expected = "http://host";
    assertEquals(expected, Urls.escape("http://////host"));
    assertEquals(expected, Urls.escape("http:/host"));
    assertEquals(expected, Urls.escape("http:\\host"));
    assertEquals(expected, Urls.escape("http:\\\\host"));
  }

  @Test public void minimalEscape_retainPlusInPath() {
    assertEquals("http://wikipedia.org/c++", Urls.escape("http://wikipedia.org/c++"));
  }

  @Test public void minimalEscape_retainPlusInQiuery() {
    assertEquals("http://wikipedia.org/?q=c++", Urls.escape("http://wikipedia.org/?q=c++"));
  }

  @Test public void minimalEncodePath() {
    verifyEscaping("/%00", "/\u0000");
    verifyEscaping("/%01", "/\u0001");
    verifyEscaping("/%02", "/\u0002");
    verifyEscaping("/%03", "/\u0003");
    verifyEscaping("/%04", "/\u0004");
    verifyEscaping("/%05", "/\u0005");
    verifyEscaping("/%06", "/\u0006");
    verifyEscaping("/%07", "/\u0007");
    verifyEscaping("/%08", "/\u0008");
    verifyEscaping("/", "/\u0009");
    verifyEscaping("/", "/\n");
    verifyEscaping("/%0B", "/\u000b");
    verifyEscaping("/", "/\u000c");
    verifyEscaping("/", "/\r");
    verifyEscaping("/%0E", "/\u000e");
    verifyEscaping("/%0F", "/\u000f");
    verifyEscaping("/%10", "/\u0010");
    verifyEscaping("/%11", "/\u0011");
    verifyEscaping("/%12", "/\u0012");
    verifyEscaping("/%13", "/\u0013");
    verifyEscaping("/%14", "/\u0014");
    verifyEscaping("/%15", "/\u0015");
    verifyEscaping("/%16", "/\u0016");
    verifyEscaping("/%17", "/\u0017");
    verifyEscaping("/%18", "/\u0018");
    verifyEscaping("/%19", "/\u0019");
    verifyEscaping("/%1A", "/\u001a");
    verifyEscaping("/%1B", "/\u001b");
    verifyEscaping("/%1C", "/\u001c");
    verifyEscaping("/%1D", "/\u001d");
    verifyEscaping("/%1E", "/\u001e");
    verifyEscaping("/%1F", "/\u001f");
    verifyEscaping("/", "/\u0020");
    verifyEscaping("/!", "/!");
    verifyEscaping("/%22", "/\"");
    verifyEscaping("/#", "/#");
    verifyEscaping("/$", "/$");
    verifyEscaping("/%25", "/%");
    verifyEscaping("/&", "/&");
    verifyEscaping("/'", "/'");
    verifyEscaping("/(", "/(");
    verifyEscaping("/)", "/)");
    verifyEscaping("/*", "/*");
    verifyEscaping("/+", "/+");
    verifyEscaping("/,", "/,");
    verifyEscaping("/-", "/-");
    verifyEscaping("/.", "/.");
    verifyEscaping("/", "/");
    verifyEscaping("/0", "/0");
    verifyEscaping("/1", "/1");
    verifyEscaping("/2", "/2");
    verifyEscaping("/3", "/3");
    verifyEscaping("/4", "/4");
    verifyEscaping("/5", "/5");
    verifyEscaping("/6", "/6");
    verifyEscaping("/7", "/7");
    verifyEscaping("/8", "/8");
    verifyEscaping("/9", "/9");
    verifyEscaping("/:", "/:");
    verifyEscaping("/;", "/;");
    verifyEscaping("/%3C", "/<");
    verifyEscaping("/=", "/=");
    verifyEscaping("/%3E", "/>");
    verifyEscaping("/?", "/?");
    verifyEscaping("/@", "/@");
    verifyEscaping("/A", "/A");
    verifyEscaping("/B", "/B");
    verifyEscaping("/C", "/C");
    verifyEscaping("/D", "/D");
    verifyEscaping("/E", "/E");
    verifyEscaping("/F", "/F");
    verifyEscaping("/G", "/G");
    verifyEscaping("/H", "/H");
    verifyEscaping("/I", "/I");
    verifyEscaping("/J", "/J");
    verifyEscaping("/K", "/K");
    verifyEscaping("/L", "/L");
    verifyEscaping("/M", "/M");
    verifyEscaping("/N", "/N");
    verifyEscaping("/O", "/O");
    verifyEscaping("/P", "/P");
    verifyEscaping("/Q", "/Q");
    verifyEscaping("/R", "/R");
    verifyEscaping("/S", "/S");
    verifyEscaping("/T", "/T");
    verifyEscaping("/U", "/U");
    verifyEscaping("/V", "/V");
    verifyEscaping("/W", "/W");
    verifyEscaping("/X", "/X");
    verifyEscaping("/Y", "/Y");
    verifyEscaping("/Z", "/Z");
    verifyEscaping("/%5B", "/[");
    verifyEscaping("/", "\\");
    verifyEscaping("/%5D", "/]");
    verifyEscaping("/%5E", "/^");
    verifyEscaping("/_", "/_");
    verifyEscaping("/%60", "/`");
    verifyEscaping("/a", "/a");
    verifyEscaping("/b", "/b");
    verifyEscaping("/c", "/c");
    verifyEscaping("/d", "/d");
    verifyEscaping("/e", "/e");
    verifyEscaping("/f", "/f");
    verifyEscaping("/g", "/g");
    verifyEscaping("/h", "/h");
    verifyEscaping("/i", "/i");
    verifyEscaping("/j", "/j");
    verifyEscaping("/k", "/k");
    verifyEscaping("/l", "/l");
    verifyEscaping("/m", "/m");
    verifyEscaping("/n", "/n");
    verifyEscaping("/o", "/o");
    verifyEscaping("/p", "/p");
    verifyEscaping("/q", "/q");
    verifyEscaping("/r", "/r");
    verifyEscaping("/s", "/s");
    verifyEscaping("/t", "/t");
    verifyEscaping("/u", "/u");
    verifyEscaping("/v", "/v");
    verifyEscaping("/w", "/w");
    verifyEscaping("/x", "/x");
    verifyEscaping("/y", "/y");
    verifyEscaping("/z", "/z");
    verifyEscaping("/%7B", "/{");
    verifyEscaping("/%7C", "/|");
    verifyEscaping("/%7D", "/}");
    verifyEscaping("/~", "/~");
    verifyEscaping("/%7F", "/\u007f");
    verifyEscaping("/%C2%80", "/\u0080");
  }

  @Test public void minimalEscapeQuery() {
    verifyEscaping("?%00=%00&%00=%00", "?\u0000=\u0000&\u0000=\u0000");
    verifyEscaping("?%01=%01&%01=%01", "?\u0001=\u0001&\u0001=\u0001");
    verifyEscaping("?%02=%02&%02=%02", "?\u0002=\u0002&\u0002=\u0002");
    verifyEscaping("?%03=%03&%03=%03", "?\u0003=\u0003&\u0003=\u0003");
    verifyEscaping("?%04=%04&%04=%04", "?\u0004=\u0004&\u0004=\u0004");
    verifyEscaping("?%05=%05&%05=%05", "?\u0005=\u0005&\u0005=\u0005");
    verifyEscaping("?%06=%06&%06=%06", "?\u0006=\u0006&\u0006=\u0006");
    verifyEscaping("?%07=%07&%07=%07", "?\u0007=\u0007&\u0007=\u0007");
    verifyEscaping("?%08=%08&%08=%08", "?\u0008=\u0008&\u0008=\u0008");
    verifyEscaping("?%09=%09&%09=", "?\u0009=\u0009&\u0009=\u0009");
    verifyEscaping("?=&=", "?\n=\n&\n=\n");
    verifyEscaping("?%0B=%0B&%0B=%0B", "?\u000b=\u000b&\u000b=\u000b");
    verifyEscaping("?%0C=%0C&%0C=", "?\u000c=\u000c&\u000c=\u000c");
    verifyEscaping("?=&=", "?\r=\r&\r=\r");
    verifyEscaping("?%0E=%0E&%0E=%0E", "?\u000e=\u000e&\u000e=\u000e");
    verifyEscaping("?%0F=%0F&%0F=%0F", "?\u000f=\u000f&\u000f=\u000f");
    verifyEscaping("?%10=%10&%10=%10", "?\u0010=\u0010&\u0010=\u0010");
    verifyEscaping("?%11=%11&%11=%11", "?\u0011=\u0011&\u0011=\u0011");
    verifyEscaping("?%12=%12&%12=%12", "?\u0012=\u0012&\u0012=\u0012");
    verifyEscaping("?%13=%13&%13=%13", "?\u0013=\u0013&\u0013=\u0013");
    verifyEscaping("?%14=%14&%14=%14", "?\u0014=\u0014&\u0014=\u0014");
    verifyEscaping("?%15=%15&%15=%15", "?\u0015=\u0015&\u0015=\u0015");
    verifyEscaping("?%16=%16&%16=%16", "?\u0016=\u0016&\u0016=\u0016");
    verifyEscaping("?%17=%17&%17=%17", "?\u0017=\u0017&\u0017=\u0017");
    verifyEscaping("?%18=%18&%18=%18", "?\u0018=\u0018&\u0018=\u0018");
    verifyEscaping("?%19=%19&%19=%19", "?\u0019=\u0019&\u0019=\u0019");
    verifyEscaping("?%1A=%1A&%1A=%1A", "?\u001a=\u001a&\u001a=\u001a");
    verifyEscaping("?%1B=%1B&%1B=%1B", "?\u001b=\u001b&\u001b=\u001b");
    verifyEscaping("?%1C=%1C&%1C=%1C", "?\u001c=\u001c&\u001c=\u001c");
    verifyEscaping("?%1D=%1D&%1D=%1D", "?\u001d=\u001d&\u001d=\u001d");
    verifyEscaping("?%1E=%1E&%1E=%1E", "?\u001e=\u001e&\u001e=\u001e");
    verifyEscaping("?%1F=%1F&%1F=%1F", "?\u001f=\u001f&\u001f=\u001f");
    verifyEscaping("?%20=%20&%20=", "?\u0020=\u0020&\u0020=\u0020");
    verifyEscaping("?!=!&!=!", "?!=!&!=!");
    verifyEscaping("?%22=%22&%22=%22", "?\"=\"&\"=\"");
    verifyEscaping("?#=%23&%23=%23", "?#=#&#=#");
    verifyEscaping("?$=$&$=$", "?$=$&$=$");
    verifyEscaping("?%25=%25&%25=%25", "?%=%&%=%");
    verifyEscaping("?&=&&&=&", "?&=&&&=&");
    verifyEscaping("?'='&'='", "?'='&'='");
    verifyEscaping("?(=(&(=(", "?(=(&(=(");
    verifyEscaping("?)=)&)=)", "?)=)&)=)");
    verifyEscaping("?*=*&*=*", "?*=*&*=*");
    verifyEscaping("?+=+&+=+", "?+=+&+=+");
    verifyEscaping("?,=,&,=,", "?,=,&,=,");
    verifyEscaping("?-=-&-=-", "?-=-&-=-");
    verifyEscaping("?.=.&.=.", "?.=.&.=.");
    verifyEscaping("?/=/&/=/", "?/=/&/=/");
    verifyEscaping("?0=0&0=0", "?0=0&0=0");
    verifyEscaping("?1=1&1=1", "?1=1&1=1");
    verifyEscaping("?2=2&2=2", "?2=2&2=2");
    verifyEscaping("?3=3&3=3", "?3=3&3=3");
    verifyEscaping("?4=4&4=4", "?4=4&4=4");
    verifyEscaping("?5=5&5=5", "?5=5&5=5");
    verifyEscaping("?6=6&6=6", "?6=6&6=6");
    verifyEscaping("?7=7&7=7", "?7=7&7=7");
    verifyEscaping("?8=8&8=8", "?8=8&8=8");
    verifyEscaping("?9=9&9=9", "?9=9&9=9");
    verifyEscaping("?:=:&:=:", "?:=:&:=:");
    verifyEscaping("?;=;&;=;", "?;=;&;=;");
    verifyEscaping("?%3C=%3C&%3C=%3C", "?<=<&<=<");
    verifyEscaping("?===&===", "?===&===");
    verifyEscaping("?%3E=%3E&%3E=%3E", "?>=>&>=>");
    verifyEscaping("??=?&?=?", "??=?&?=?");
    verifyEscaping("?@=@&@=@", "?@=@&@=@");
    verifyEscaping("?A=A&A=A", "?A=A&A=A");
    verifyEscaping("?B=B&B=B", "?B=B&B=B");
    verifyEscaping("?C=C&C=C", "?C=C&C=C");
    verifyEscaping("?D=D&D=D", "?D=D&D=D");
    verifyEscaping("?E=E&E=E", "?E=E&E=E");
    verifyEscaping("?F=F&F=F", "?F=F&F=F");
    verifyEscaping("?G=G&G=G", "?G=G&G=G");
    verifyEscaping("?H=H&H=H", "?H=H&H=H");
    verifyEscaping("?I=I&I=I", "?I=I&I=I");
    verifyEscaping("?J=J&J=J", "?J=J&J=J");
    verifyEscaping("?K=K&K=K", "?K=K&K=K");
    verifyEscaping("?L=L&L=L", "?L=L&L=L");
    verifyEscaping("?M=M&M=M", "?M=M&M=M");
    verifyEscaping("?N=N&N=N", "?N=N&N=N");
    verifyEscaping("?O=O&O=O", "?O=O&O=O");
    verifyEscaping("?P=P&P=P", "?P=P&P=P");
    verifyEscaping("?Q=Q&Q=Q", "?Q=Q&Q=Q");
    verifyEscaping("?R=R&R=R", "?R=R&R=R");
    verifyEscaping("?S=S&S=S", "?S=S&S=S");
    verifyEscaping("?T=T&T=T", "?T=T&T=T");
    verifyEscaping("?U=U&U=U", "?U=U&U=U");
    verifyEscaping("?V=V&V=V", "?V=V&V=V");
    verifyEscaping("?W=W&W=W", "?W=W&W=W");
    verifyEscaping("?X=X&X=X", "?X=X&X=X");
    verifyEscaping("?Y=Y&Y=Y", "?Y=Y&Y=Y");
    verifyEscaping("?Z=Z&Z=Z", "?Z=Z&Z=Z");
    verifyEscaping("?%5B=%5B&%5B=%5B", "?[=[&[=[");
    verifyEscaping("?%5C=%5C&%5C=%5C", "?\\=\\&\\=\\");
    verifyEscaping("?%5D=%5D&%5D=%5D", "?]=]&]=]");
    verifyEscaping("?%5E=%5E&%5E=%5E", "?^=^&^=^");
    verifyEscaping("?_=_&_=_", "?_=_&_=_");
    verifyEscaping("?%60=%60&%60=%60", "?`=`&`=`");
    verifyEscaping("?a=a&a=a", "?a=a&a=a");
    verifyEscaping("?b=b&b=b", "?b=b&b=b");
    verifyEscaping("?c=c&c=c", "?c=c&c=c");
    verifyEscaping("?d=d&d=d", "?d=d&d=d");
    verifyEscaping("?e=e&e=e", "?e=e&e=e");
    verifyEscaping("?f=f&f=f", "?f=f&f=f");
    verifyEscaping("?g=g&g=g", "?g=g&g=g");
    verifyEscaping("?h=h&h=h", "?h=h&h=h");
    verifyEscaping("?i=i&i=i", "?i=i&i=i");
    verifyEscaping("?j=j&j=j", "?j=j&j=j");
    verifyEscaping("?k=k&k=k", "?k=k&k=k");
    verifyEscaping("?l=l&l=l", "?l=l&l=l");
    verifyEscaping("?m=m&m=m", "?m=m&m=m");
    verifyEscaping("?n=n&n=n", "?n=n&n=n");
    verifyEscaping("?o=o&o=o", "?o=o&o=o");
    verifyEscaping("?p=p&p=p", "?p=p&p=p");
    verifyEscaping("?q=q&q=q", "?q=q&q=q");
    verifyEscaping("?r=r&r=r", "?r=r&r=r");
    verifyEscaping("?s=s&s=s", "?s=s&s=s");
    verifyEscaping("?t=t&t=t", "?t=t&t=t");
    verifyEscaping("?u=u&u=u", "?u=u&u=u");
    verifyEscaping("?v=v&v=v", "?v=v&v=v");
    verifyEscaping("?w=w&w=w", "?w=w&w=w");
    verifyEscaping("?x=x&x=x", "?x=x&x=x");
    verifyEscaping("?y=y&y=y", "?y=y&y=y");
    verifyEscaping("?z=z&z=z", "?z=z&z=z");
    verifyEscaping("?%7B=%7B&%7B=%7B", "?{={&{={");
    verifyEscaping("?%7C=%7C&%7C=%7C", "?|=|&|=|");
    verifyEscaping("?%7D=%7D&%7D=%7D", "?}=}&}=}");
    verifyEscaping("?~=~&~=~", "?~=~&~=~");
    verifyEscaping("?%7F=%7F&%7F=%7F", "?\u007f=\u007f&\u007f=\u007f");
    verifyEscaping("?%C2%80=%C2%80&%C2%80=%C2%80", "?\u0080=\u0080&\u0080=\u0080");

    verifyEscaping("?%2e", "?%2e");
    verifyEscaping("?%25zz", "?%zz");
    verifyEscaping("?+==", "?+==");
  }

  @Test public void minimalEscapeFragment() {
    verifyEscaping("#%00", "#\u0000");
    verifyEscaping("#%01", "#\u0001");
    verifyEscaping("#%02", "#\u0002");
    verifyEscaping("#%03", "#\u0003");
    verifyEscaping("#%04", "#\u0004");
    verifyEscaping("#%05", "#\u0005");
    verifyEscaping("#%06", "#\u0006");
    verifyEscaping("#%07", "#\u0007");
    verifyEscaping("#%08", "#\u0008");
    verifyEscaping("#", "#\u0009");
    verifyEscaping("#", "#\n");
    verifyEscaping("#%0B", "#\u000b");
    verifyEscaping("#", "#\u000c");
    verifyEscaping("#", "#\r");
    verifyEscaping("#%0E", "#\u000e");
    verifyEscaping("#%0F", "#\u000f");
    verifyEscaping("#%10", "#\u0010");
    verifyEscaping("#%11", "#\u0011");
    verifyEscaping("#%12", "#\u0012");
    verifyEscaping("#%13", "#\u0013");
    verifyEscaping("#%14", "#\u0014");
    verifyEscaping("#%15", "#\u0015");
    verifyEscaping("#%16", "#\u0016");
    verifyEscaping("#%17", "#\u0017");
    verifyEscaping("#%18", "#\u0018");
    verifyEscaping("#%19", "#\u0019");
    verifyEscaping("#%1A", "#\u001a");
    verifyEscaping("#%1B", "#\u001b");
    verifyEscaping("#%1C", "#\u001c");
    verifyEscaping("#%1D", "#\u001d");
    verifyEscaping("#%1E", "#\u001e");
    verifyEscaping("#%1F", "#\u001f");
    verifyEscaping("#", "#\u0020");
    verifyEscaping("#!", "#!");
    verifyEscaping("#%22", "#\"");
    verifyEscaping("#%23", "##");
    verifyEscaping("#$", "#$");
    verifyEscaping("#%25", "#%");
    verifyEscaping("#&", "#&");
    verifyEscaping("#'", "#'");
    verifyEscaping("#(", "#(");
    verifyEscaping("#)", "#)");
    verifyEscaping("#*", "#*");
    verifyEscaping("#+", "#+");
    verifyEscaping("#,", "#,");
    verifyEscaping("#-", "#-");
    verifyEscaping("#.", "#.");
    verifyEscaping("#/", "#/");
    verifyEscaping("#0", "#0");
    verifyEscaping("#1", "#1");
    verifyEscaping("#2", "#2");
    verifyEscaping("#3", "#3");
    verifyEscaping("#4", "#4");
    verifyEscaping("#5", "#5");
    verifyEscaping("#6", "#6");
    verifyEscaping("#7", "#7");
    verifyEscaping("#8", "#8");
    verifyEscaping("#9", "#9");
    verifyEscaping("#:", "#:");
    verifyEscaping("#;", "#;");
    verifyEscaping("#%3C", "#<");
    verifyEscaping("#=", "#=");
    verifyEscaping("#%3E", "#>");
    verifyEscaping("#?", "#?");
    verifyEscaping("#@", "#@");
    verifyEscaping("#A", "#A");
    verifyEscaping("#B", "#B");
    verifyEscaping("#C", "#C");
    verifyEscaping("#D", "#D");
    verifyEscaping("#E", "#E");
    verifyEscaping("#F", "#F");
    verifyEscaping("#G", "#G");
    verifyEscaping("#H", "#H");
    verifyEscaping("#I", "#I");
    verifyEscaping("#J", "#J");
    verifyEscaping("#K", "#K");
    verifyEscaping("#L", "#L");
    verifyEscaping("#M", "#M");
    verifyEscaping("#N", "#N");
    verifyEscaping("#O", "#O");
    verifyEscaping("#P", "#P");
    verifyEscaping("#Q", "#Q");
    verifyEscaping("#R", "#R");
    verifyEscaping("#S", "#S");
    verifyEscaping("#T", "#T");
    verifyEscaping("#U", "#U");
    verifyEscaping("#V", "#V");
    verifyEscaping("#W", "#W");
    verifyEscaping("#X", "#X");
    verifyEscaping("#Y", "#Y");
    verifyEscaping("#Z", "#Z");
    verifyEscaping("#%5B", "#[");
    verifyEscaping("#%5C", "#\\");
    verifyEscaping("#%5D", "#]");
    verifyEscaping("#%5E", "#^");
    verifyEscaping("#_", "#_");
    verifyEscaping("#%60", "#`");
    verifyEscaping("#a", "#a");
    verifyEscaping("#b", "#b");
    verifyEscaping("#c", "#c");
    verifyEscaping("#d", "#d");
    verifyEscaping("#e", "#e");
    verifyEscaping("#f", "#f");
    verifyEscaping("#g", "#g");
    verifyEscaping("#h", "#h");
    verifyEscaping("#i", "#i");
    verifyEscaping("#j", "#j");
    verifyEscaping("#k", "#k");
    verifyEscaping("#l", "#l");
    verifyEscaping("#m", "#m");
    verifyEscaping("#n", "#n");
    verifyEscaping("#o", "#o");
    verifyEscaping("#p", "#p");
    verifyEscaping("#q", "#q");
    verifyEscaping("#r", "#r");
    verifyEscaping("#s", "#s");
    verifyEscaping("#t", "#t");
    verifyEscaping("#u", "#u");
    verifyEscaping("#v", "#v");
    verifyEscaping("#w", "#w");
    verifyEscaping("#x", "#x");
    verifyEscaping("#y", "#y");
    verifyEscaping("#z", "#z");
    verifyEscaping("#%7B", "#{");
    verifyEscaping("#%7C", "#|");
    verifyEscaping("#%7D", "#}");
    verifyEscaping("#~", "#~");
    verifyEscaping("#%7F", "#\u007f");
    verifyEscaping("#%C2%80", "#\u0080");
    verifyEscaping("#%C2%81", "#\u0081");
    verifyEscaping("#%C2%82", "#\u0082");
    verifyEscaping("#%C2%83", "#\u0083");
    verifyEscaping("#%C2%84", "#\u0084");

    verifyEscaping("#%2e", "#%2e");
    verifyEscaping("#%25zz", "#%zz");
  }

  private void verifyEscaping(String expected, String input) {
    expected = "http://host" + expected;
    input = "http://host" + input;
    try {
      new URI(expected);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
    assertEquals(expected, Urls.escape(input));
  }
}