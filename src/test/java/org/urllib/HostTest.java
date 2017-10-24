package org.urllib;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class HostTest {

  @Test public void generalValidation_dontAllowLeadingDots() {
    assertInvalid(".com");
    assertInvalid(".1.1.1.1");
  }

  @Test public void generalValidation_dontAllowEmptySegments() {
    assertInvalid("host..com");
    assertInvalid("1.1..1.1");
  }

  @Test public void ipv4_removeTrailingPeriod() {
    assertEquals("255.255.255.255", Host.forString("255.255.255.255.").name());
  }

  @Test public void ipv4_percentDecodeBeforeParsing() {
    assertEquals("1.1.1.1", Host.forString("%31.%31.%31.%31").name());
    assertEquals("1.1.1.1", Host.forString("1%2e1%2e1%2e1").name());
  }

  @Test public void ipv4_disambiguateUnicodeFirst() {
    assertEquals("1.1.1.1", Host.forString("１。１。１。１").name());
  }

  @Test public void ipv4() {
    assertEquals("1.1.1.1", Host.forString("1.1.1.1").name());
    assertEquals("0.0.0.1", Host.forString("0.0.0.1").name());
    assertEquals("0.0.0.0", Host.forString("0.0.0.0").name());
    assertEquals("255.255.255.255", Host.forString("255.255.255.255").name());
  }

  @Test public void ipv4_wrongNumberSegments() {
    assertInvalid("1");
    assertInvalid("1.1");
    assertInvalid("1.1.1");
    assertInvalid("1.1.1.1.1");
  }

  @Test public void ipv4_outOfRange() {
    assertInvalid("-1.1.1.1");
    assertInvalid("1.1.1.256");
    assertInvalid("1.1.1.1000");
  }

  @Test public void ipv4_notAllNumeric() {
    assertInvalid("A.1.1.1");
    assertInvalid("1.1.A.1");
  }

  @Test public void ipv4_dontAllowLeadingZero() {
    assertInvalid("4.89.8.05");
    assertInvalid("01.1.1.1");
  }

  @Test public void ipv6() {
    assertEquals("::", Host.forString("::").name());
    assertEquals("200:100::", Host.forString("[0200:0100:0:0::]").name());
    assertEquals("2::", Host.forString("[2::]").name());
    assertEquals("::2", Host.forString("[::2]").name());
    assertEquals("1::2", Host.forString("[1::2]").name());
    assertEquals("2001:db8::ff00:42:8329", Host.forString("2001:db8:0:0:0:ff00:42:8329").name());
    assertEquals("1111:2222:3333:4444:5555:6666:7777:8888",
        Host.forString("1111:2222:3333:4444:5555:6666:7777:8888").name());
    assertEquals("1:2:3:4:5:6:7:8", Host.forString("1:2:3:4:5:6:7:8").name());
  }

  @Test public void ipv6_empty() {
    String expected = "::";
    assertEquals(expected, Host.forString("::").name());
    assertEquals(expected, Host.forString("::0").name());
    assertEquals(expected, Host.forString("::0:0").name());
    assertEquals(expected, Host.forString("0:0:0:0:0:0:0:0").name());
  }

  // From OkHttp
  @Test public void ipv6_differentFormats() {
    // Multiple representations of the same address; see http://tools.ietf.org/html/rfc5952.
    String expected = "2001:db8::1:0:0:1";
    assertEquals(expected, Host.forString("[2001:db8:0:0:1:0:0:1]").name());
    assertEquals(expected, Host.forString("[2001:0db8:0:0:1:0:0:1]").name());
    assertEquals(expected, Host.forString("[2001:db8::1:0:0:1]").name());
    assertEquals(expected, Host.forString("[2001:db8::0:1:0:0:1]").name());
    assertEquals(expected, Host.forString("[2001:0db8::1:0:0:1]").name());
    assertEquals(expected, Host.forString("[2001:db8:0:0:1::1]").name());
    assertEquals(expected, Host.forString("[2001:db8:0000:0:1::1]").name());
    assertEquals(expected, Host.forString("[2001:DB8:0:0:1::1]").name());
  }

  // From OkHttp
  @Test public void ipv6_leadingCompression() {
    assertEquals("::1", Host.forString("[::0001]").name());
    assertEquals("::1", Host.forString("[0000::0001]").name());
    assertEquals("::1", Host.forString("[0000:0000:0000:0000:0000:0000:0000:0001]").name());
    assertEquals("::1", Host.forString("[0000:0000:0000:0000:0000:0000::0001]").name());
  }

  // From OkHttp
  @Test public void ipv6_trailingCompression() {
    assertEquals("1::", Host.forString("[0001:0000::]").name());
    assertEquals("1::", Host.forString("[0001::0000]").name());
    assertEquals("1::", Host.forString("[0001::]").name());
    assertEquals("1::", Host.forString("[1::]").name());
  }

  // From OkHttp
  @Test public void ipv6_tooManyDigitsInGroup() {
    assertInvalid("[00000:0000:0000:0000:0000:0000:0000:0001]");
    assertInvalid("[::00001]");
  }

  // From OkHttp
  @Test public void ipv6_misplacedColons() {
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
  @Test public void ipv6_tooManyGroups() {
    assertInvalid("[0000:0000:0000:0000:0000:0000:0000:0000:0001]");
  }

  @Test public void ipv6_tooFewGroups() {
    assertInvalid("[36:5361]");
  }

  // From OkHttp
  @Test public void ipv6_tooMuchCompression() {
    assertInvalid("[0000::0000:0000:0000:0000::0001]");
    assertInvalid("[::0000:0000:0000:0000::0001]");
  }

  // From OkHttp
  @Test public void ipv6_canonicalForm() {
    assertEquals("abcd:ef01:2345:6789:abcd:ef01:2345:6789",
        Host.forString("[abcd:ef01:2345:6789:abcd:ef01:2345:6789]").name());
    assertEquals("a::b:0:0:0", Host.forString("[a:0:0:0:b:0:0:0]").name());
    assertEquals("a:b:0:0:c::", Host.forString("[a:b:0:0:c:0:0:0]").name());
    assertEquals("a:b::c:0:0", Host.forString("[a:b:0:0:0:c:0:0]").name());
    assertEquals("a::b:0:0:0", Host.forString("[a:0:0:0:b:0:0:0]").name());
    assertEquals("::a:b:0:0:0", Host.forString("[0:0:0:a:b:0:0:0]").name());
    assertEquals("::a:0:0:0:b", Host.forString("[0:0:0:a:0:0:0:b]").name());
    assertEquals("0:a:b:c:d:e:f:1", Host.forString("[0:a:b:c:d:e:f:1]").name());
    assertEquals("a:b:c:d:e:f:1:0", Host.forString("[a:b:c:d:e:f:1:0]").name());
    assertEquals("ff01::101", Host.forString("[FF01:0:0:0:0:0:0:101]").name());
    assertEquals("2001:db8::1", Host.forString("[2001:db8::1]").name());
    assertEquals("2001:db8::2:1", Host.forString("[2001:db8:0:0:0:0:2:1]").name());
    assertEquals("2001:db8:0:1:1:1:1:1", Host.forString("[2001:db8:0:1:1:1:1:1]").name());
    assertEquals("2001:db8::1:0:0:1", Host.forString("[2001:db8:0:0:1:0:0:1]").name());
    assertEquals("2001:0:0:1::1", Host.forString("[2001:0:0:1:0:0:0:1]").name());
    assertEquals("1::", Host.forString("[1:0:0:0:0:0:0:0]").name());
    assertEquals("::1", Host.forString("[0:0:0:0:0:0:0:1]").name());
    assertEquals("::", Host.forString("[0:0:0:0:0:0:0:0]").name());
  }

  private void assertInvalid(String host) {
    try {
      Host.forString(host);
      fail("Expected IllegalArgumentException for: " + host);
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("Invalid hostname"));
    }
  }
}