package org.urllib.internal.authority;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.urllib.internal.authority.Hosts;

public class Ip6Test {

  @Test public void ipv6() {
    assertEquals("[::]", Hosts.parse("::").name());
    assertEquals("[200:100::]", Hosts.parse("[0200:0100:0:0::]").name());
    assertEquals("[2::]", Hosts.parse("[2::]").name());
    assertEquals("[::2]", Hosts.parse("[::2]").name());
    assertEquals("[1::2]", Hosts.parse("[1::2]").name());
    assertEquals("[2001:db8::ff00:42:8329]", Hosts.parse("2001:db8:0:0:0:ff00:42:8329").name());
    assertEquals("[1111:2222:3333:4444:5555:6666:7777:8888]",
        Hosts.parse("1111:2222:3333:4444:5555:6666:7777:8888").name());
    assertEquals("[1:2:3:4:5:6:7:8]", Hosts.parse("1:2:3:4:5:6:7:8").name());
  }

  @Test public void empty() {
    String expected = "[::]";
    assertEquals(expected, Hosts.parse("::").name());
    assertEquals(expected, Hosts.parse("::0").name());
    assertEquals(expected, Hosts.parse("::0:0").name());
    assertEquals(expected, Hosts.parse("0:0:0:0:0:0:0:0").name());
  }

  // From OkHttp
  @Test public void differentFormats() {
    // Multiple representations of the same address; see http://tools.ietf.org/html/rfc5952.
    String expected = "[2001:db8::1:0:0:1]";
    assertEquals(expected, Hosts.parse("[2001:db8:0:0:1:0:0:1]").name());
    assertEquals(expected, Hosts.parse("[2001:0db8:0:0:1:0:0:1]").name());
    assertEquals(expected, Hosts.parse("[2001:db8::1:0:0:1]").name());
    assertEquals(expected, Hosts.parse("[2001:db8::0:1:0:0:1]").name());
    assertEquals(expected, Hosts.parse("[2001:0db8::1:0:0:1]").name());
    assertEquals(expected, Hosts.parse("[2001:db8:0:0:1::1]").name());
    assertEquals(expected, Hosts.parse("[2001:db8:0000:0:1::1]").name());
    assertEquals(expected, Hosts.parse("[2001:DB8:0:0:1::1]").name());
  }

  // From OkHttp
  @Test public void leadingCompression() {
    assertEquals("[::1]", Hosts.parse("[::0001]").name());
    assertEquals("[::1]", Hosts.parse("[0000::0001]").name());
    assertEquals("[::1]", Hosts.parse("[0000:0000:0000:0000:0000:0000:0000:0001]").name());
    assertEquals("[::1]", Hosts.parse("[0000:0000:0000:0000:0000:0000::0001]").name());
  }

  // From OkHttp
  @Test public void trailingCompression() {
    assertEquals("[1::]", Hosts.parse("[0001:0000::]").name());
    assertEquals("[1::]", Hosts.parse("[0001::0000]").name());
    assertEquals("[1::]", Hosts.parse("[0001::]").name());
    assertEquals("[1::]", Hosts.parse("[1::]").name());
  }

  // From OkHttp
  @Test public void tooManyDigitsInGroup() {
    assertInvalid("[00000:0000:0000:0000:0000:0000:0000:0001]");
    assertInvalid("[::00001]");
  }

  // From OkHttp
  @Test public void misplacedColons() {
    assertInvalid("[:0000:0000:0000:0000:0000:0000:0000:0001]");
    assertInvalid("[:::0000:0000:0000:0000:0000:0000:0000:0001]");
    assertInvalid("[:1]");
    assertInvalid("[:::1]");
    assertInvalid("[0000:0000:0000:0000:0000:0000:0001:]");
    assertInvalid("[0000:0000:0000:0000:0000:0000:0000:0001:]");
    assertInvalid("[0000:0000:0000:0000:0000:0000:0000:0001::]");
    assertInvalid("[0000:0000:0000:0000:0000:0000:0000:0001:::]");
    assertInvalid("[1:]");
    assertInvalid("[1:::]");
    assertInvalid("[1:::1]");
    assertInvalid("[0000:0000:0000:0000::0000:0000:0000:0001]");
  }

  // From OkHttp
  @Test public void tooManyGroups() {
    assertInvalid("[0000:0000:0000:0000:0000:0000:0000:0000:0001]");
  }

  @Test public void tooFewGroups() {
    assertInvalid("[36:5361]");
  }

  // From OkHttp
  @Test public void tooMuchCompression() {
    assertInvalid("[0000::0000:0000:0000:0000::0001]");
    assertInvalid("[::0000:0000:0000:0000::0001]");
  }

  // From OkHttp
  @Test public void canonicalForm() {
    assertEquals("[abcd:ef01:2345:6789:abcd:ef01:2345:6789]",
        Hosts.parse("[abcd:ef01:2345:6789:abcd:ef01:2345:6789]").name());
    assertEquals("[a::b:0:0:0]", Hosts.parse("[a:0:0:0:b:0:0:0]").name());
    assertEquals("[a:b:0:0:c::]", Hosts.parse("[a:b:0:0:c:0:0:0]").name());
    assertEquals("[a:b::c:0:0]", Hosts.parse("[a:b:0:0:0:c:0:0]").name());
    assertEquals("[a::b:0:0:0]", Hosts.parse("[a:0:0:0:b:0:0:0]").name());
    assertEquals("[::a:b:0:0:0]", Hosts.parse("[0:0:0:a:b:0:0:0]").name());
    assertEquals("[::a:0:0:0:b]", Hosts.parse("[0:0:0:a:0:0:0:b]").name());
    assertEquals("[0:a:b:c:d:e:f:1]", Hosts.parse("[0:a:b:c:d:e:f:1]").name());
    assertEquals("[a:b:c:d:e:f:1:0]", Hosts.parse("[a:b:c:d:e:f:1:0]").name());
    assertEquals("[ff01::101]", Hosts.parse("[FF01:0:0:0:0:0:0:101]").name());
    assertEquals("[2001:db8::1]", Hosts.parse("[2001:db8::1]").name());
    assertEquals("[2001:db8::2:1]", Hosts.parse("[2001:db8:0:0:0:0:2:1]").name());
    assertEquals("[2001:db8:0:1:1:1:1:1]", Hosts.parse("[2001:db8:0:1:1:1:1:1]").name());
    assertEquals("[2001:db8::1:0:0:1]", Hosts.parse("[2001:db8:0:0:1:0:0:1]").name());
    assertEquals("[2001:0:0:1::1]", Hosts.parse("[2001:0:0:1:0:0:0:1]").name());
    assertEquals("[1::]", Hosts.parse("[1:0:0:0:0:0:0:0]").name());
    assertEquals("[::1]", Hosts.parse("[0:0:0:0:0:0:0:1]").name());
    assertEquals("[::]", Hosts.parse("[0:0:0:0:0:0:0:0]").name());
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