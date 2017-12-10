package org.urllib.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

public class PercentEncoderTest {

  private static final String ASCII = "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008"
      + "\u0009\n\u000b\u000c\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017"
      + "\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f !\"#$%&'()*+,-./0123456789"
      + ":;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u007f";

  // Semicolons are nasty. RFC 2396 has the idea of a "path parameter"
  // that uses the semicolon. In queries, some servers will split
  // at both the ampersand *and* the semicolon.
  @Test public void encodeSemicolons_inPathAndQuery() {
    assertEquals("%3B", PercentEncoder.encodePathSegment(";"));
    assertEquals("%3B", PercentEncoder.encodeQueryComponent(";"));
  }

  @Test public void plusIsNotSpace_whenEncoding() {
    assertEquals("+", PercentEncoder.encodePathSegment("+"));
    assertEquals("%2B", PercentEncoder.encodeQueryComponent("+"));
    assertEquals("%2B", PercentEncoder.encodeQueryComponentNoPlusForSpace("+"));
    assertEquals("+", PercentEncoder.encodeFragment("+"));
  }

  @Test public void spaceIsPlus_onlyInQuery() {
    assertEquals("%20", PercentEncoder.encodePathSegment(" "));
    assertEquals("+", PercentEncoder.encodeQueryComponent(" "));
    assertEquals("%20", PercentEncoder.encodeQueryComponentNoPlusForSpace(" "));
    assertEquals("%20", PercentEncoder.encodeFragment(" "));
  }

  @Test public void encodedSegments_acceptedByJavaNetUri() throws URISyntaxException {
    String url = "http://host"
        + '/' + PercentEncoder.encodePathSegment(ASCII)
        + '?' + PercentEncoder.encodeQueryComponentNoPlusForSpace(ASCII)
        + '#' + PercentEncoder.encodeFragment(ASCII);
    URI uri = new URI(url);
    assertEquals('/' + ASCII, uri.getPath());
    assertEquals(ASCII, uri.getQuery());
    assertEquals(ASCII, uri.getFragment());
  }

  @Test public void rfc3986_pathEncodingRequired() {
    assertEquals("%00", PercentEncoder.encodePathSegment("\u0000"));
    assertEquals("%01", PercentEncoder.encodePathSegment("\u0001"));
    assertEquals("%02", PercentEncoder.encodePathSegment("\u0002"));
    assertEquals("%03", PercentEncoder.encodePathSegment("\u0003"));
    assertEquals("%04", PercentEncoder.encodePathSegment("\u0004"));
    assertEquals("%05", PercentEncoder.encodePathSegment("\u0005"));
    assertEquals("%06", PercentEncoder.encodePathSegment("\u0006"));
    assertEquals("%07", PercentEncoder.encodePathSegment("\u0007"));
    assertEquals("%08", PercentEncoder.encodePathSegment("\u0008"));
    assertEquals("%09", PercentEncoder.encodePathSegment("\u0009"));
    assertEquals("%0A", PercentEncoder.encodePathSegment("\n"));
    assertEquals("%0B", PercentEncoder.encodePathSegment("\u000b"));
    assertEquals("%0C", PercentEncoder.encodePathSegment("\u000c"));
    assertEquals("%0D", PercentEncoder.encodePathSegment("\r"));
    assertEquals("%0E", PercentEncoder.encodePathSegment("\u000e"));
    assertEquals("%0F", PercentEncoder.encodePathSegment("\u000f"));
    assertEquals("%10", PercentEncoder.encodePathSegment("\u0010"));
    assertEquals("%11", PercentEncoder.encodePathSegment("\u0011"));
    assertEquals("%12", PercentEncoder.encodePathSegment("\u0012"));
    assertEquals("%13", PercentEncoder.encodePathSegment("\u0013"));
    assertEquals("%14", PercentEncoder.encodePathSegment("\u0014"));
    assertEquals("%15", PercentEncoder.encodePathSegment("\u0015"));
    assertEquals("%16", PercentEncoder.encodePathSegment("\u0016"));
    assertEquals("%17", PercentEncoder.encodePathSegment("\u0017"));
    assertEquals("%18", PercentEncoder.encodePathSegment("\u0018"));
    assertEquals("%19", PercentEncoder.encodePathSegment("\u0019"));
    assertEquals("%1A", PercentEncoder.encodePathSegment("\u001a"));
    assertEquals("%1B", PercentEncoder.encodePathSegment("\u001b"));
    assertEquals("%1C", PercentEncoder.encodePathSegment("\u001c"));
    assertEquals("%1D", PercentEncoder.encodePathSegment("\u001d"));
    assertEquals("%1E", PercentEncoder.encodePathSegment("\u001e"));
    assertEquals("%1F", PercentEncoder.encodePathSegment("\u001f"));
    assertEquals("%20", PercentEncoder.encodePathSegment("\u0020"));
    assertEquals("%22", PercentEncoder.encodePathSegment("\""));
    assertEquals("%23", PercentEncoder.encodePathSegment("#"));
    assertEquals("%25", PercentEncoder.encodePathSegment("%"));
    assertEquals("%2F", PercentEncoder.encodePathSegment("/"));
    assertEquals("%3C", PercentEncoder.encodePathSegment("<"));
    assertEquals("%3E", PercentEncoder.encodePathSegment(">"));
    assertEquals("%3F", PercentEncoder.encodePathSegment("?"));
    assertEquals("%5B", PercentEncoder.encodePathSegment("["));
    assertEquals("%5C", PercentEncoder.encodePathSegment("\\"));
    assertEquals("%5D", PercentEncoder.encodePathSegment("]"));
    assertEquals("%5E", PercentEncoder.encodePathSegment("^"));
    assertEquals("%60", PercentEncoder.encodePathSegment("`"));
    assertEquals("%7B", PercentEncoder.encodePathSegment("{"));
    assertEquals("%7C", PercentEncoder.encodePathSegment("|"));
    assertEquals("%7D", PercentEncoder.encodePathSegment("}"));
    assertEquals("%7F", PercentEncoder.encodePathSegment("\u007f"));
  }

  @Test public void rfc3986_pathEncodingOptional() {
    assertMaybeEncoded('!', PercentEncoder.encodePathSegment("!"));
    assertMaybeEncoded('$', PercentEncoder.encodePathSegment("$"));
    assertMaybeEncoded('&', PercentEncoder.encodePathSegment("&"));
    assertMaybeEncoded('\'', PercentEncoder.encodePathSegment("'"));
    assertMaybeEncoded('(', PercentEncoder.encodePathSegment("("));
    assertMaybeEncoded(')', PercentEncoder.encodePathSegment(")"));
    assertMaybeEncoded('*', PercentEncoder.encodePathSegment("*"));
    assertMaybeEncoded('+', PercentEncoder.encodePathSegment("+"));
    assertMaybeEncoded(',', PercentEncoder.encodePathSegment(","));
    assertMaybeEncoded(':', PercentEncoder.encodePathSegment(":"));
    assertMaybeEncoded(';', PercentEncoder.encodePathSegment(";"));
    assertMaybeEncoded('=', PercentEncoder.encodePathSegment("="));
    assertMaybeEncoded('@', PercentEncoder.encodePathSegment("@"));
  }

  @Test public void rfc3986_pathEncodingNotRecommended() {
    assertEquals("-", PercentEncoder.encodePathSegment("-"));
    assertEquals(".", PercentEncoder.encodePathSegment("."));
    assertEquals("_", PercentEncoder.encodePathSegment("_"));
    assertEquals("~", PercentEncoder.encodePathSegment("~"));

    assertEquals("0", PercentEncoder.encodePathSegment("0"));
    assertEquals("1", PercentEncoder.encodePathSegment("1"));
    assertEquals("2", PercentEncoder.encodePathSegment("2"));
    assertEquals("3", PercentEncoder.encodePathSegment("3"));
    assertEquals("4", PercentEncoder.encodePathSegment("4"));
    assertEquals("5", PercentEncoder.encodePathSegment("5"));
    assertEquals("6", PercentEncoder.encodePathSegment("6"));
    assertEquals("7", PercentEncoder.encodePathSegment("7"));
    assertEquals("8", PercentEncoder.encodePathSegment("8"));
    assertEquals("9", PercentEncoder.encodePathSegment("9"));
    assertEquals("A", PercentEncoder.encodePathSegment("A"));
    assertEquals("B", PercentEncoder.encodePathSegment("B"));
    assertEquals("C", PercentEncoder.encodePathSegment("C"));
    assertEquals("D", PercentEncoder.encodePathSegment("D"));
    assertEquals("E", PercentEncoder.encodePathSegment("E"));
    assertEquals("F", PercentEncoder.encodePathSegment("F"));
    assertEquals("G", PercentEncoder.encodePathSegment("G"));
    assertEquals("H", PercentEncoder.encodePathSegment("H"));
    assertEquals("I", PercentEncoder.encodePathSegment("I"));
    assertEquals("J", PercentEncoder.encodePathSegment("J"));
    assertEquals("K", PercentEncoder.encodePathSegment("K"));
    assertEquals("L", PercentEncoder.encodePathSegment("L"));
    assertEquals("M", PercentEncoder.encodePathSegment("M"));
    assertEquals("N", PercentEncoder.encodePathSegment("N"));
    assertEquals("O", PercentEncoder.encodePathSegment("O"));
    assertEquals("P", PercentEncoder.encodePathSegment("P"));
    assertEquals("Q", PercentEncoder.encodePathSegment("Q"));
    assertEquals("R", PercentEncoder.encodePathSegment("R"));
    assertEquals("S", PercentEncoder.encodePathSegment("S"));
    assertEquals("T", PercentEncoder.encodePathSegment("T"));
    assertEquals("U", PercentEncoder.encodePathSegment("U"));
    assertEquals("V", PercentEncoder.encodePathSegment("V"));
    assertEquals("W", PercentEncoder.encodePathSegment("W"));
    assertEquals("X", PercentEncoder.encodePathSegment("X"));
    assertEquals("Y", PercentEncoder.encodePathSegment("Y"));
    assertEquals("Z", PercentEncoder.encodePathSegment("Z"));
    assertEquals("a", PercentEncoder.encodePathSegment("a"));
    assertEquals("b", PercentEncoder.encodePathSegment("b"));
    assertEquals("c", PercentEncoder.encodePathSegment("c"));
    assertEquals("d", PercentEncoder.encodePathSegment("d"));
    assertEquals("e", PercentEncoder.encodePathSegment("e"));
    assertEquals("f", PercentEncoder.encodePathSegment("f"));
    assertEquals("g", PercentEncoder.encodePathSegment("g"));
    assertEquals("h", PercentEncoder.encodePathSegment("h"));
    assertEquals("i", PercentEncoder.encodePathSegment("i"));
    assertEquals("j", PercentEncoder.encodePathSegment("j"));
    assertEquals("k", PercentEncoder.encodePathSegment("k"));
    assertEquals("l", PercentEncoder.encodePathSegment("l"));
    assertEquals("m", PercentEncoder.encodePathSegment("m"));
    assertEquals("n", PercentEncoder.encodePathSegment("n"));
    assertEquals("o", PercentEncoder.encodePathSegment("o"));
    assertEquals("p", PercentEncoder.encodePathSegment("p"));
    assertEquals("q", PercentEncoder.encodePathSegment("q"));
    assertEquals("r", PercentEncoder.encodePathSegment("r"));
    assertEquals("s", PercentEncoder.encodePathSegment("s"));
    assertEquals("t", PercentEncoder.encodePathSegment("t"));
    assertEquals("u", PercentEncoder.encodePathSegment("u"));
    assertEquals("v", PercentEncoder.encodePathSegment("v"));
    assertEquals("w", PercentEncoder.encodePathSegment("w"));
    assertEquals("x", PercentEncoder.encodePathSegment("x"));
    assertEquals("y", PercentEncoder.encodePathSegment("y"));
    assertEquals("z", PercentEncoder.encodePathSegment("z"));
  }


  @Test public void rfc3986_queryEncodingRequired() {
    assertEquals("%00", PercentEncoder.encodeQueryComponent("\u0000"));
    assertEquals("%01", PercentEncoder.encodeQueryComponent("\u0001"));
    assertEquals("%02", PercentEncoder.encodeQueryComponent("\u0002"));
    assertEquals("%03", PercentEncoder.encodeQueryComponent("\u0003"));
    assertEquals("%04", PercentEncoder.encodeQueryComponent("\u0004"));
    assertEquals("%05", PercentEncoder.encodeQueryComponent("\u0005"));
    assertEquals("%06", PercentEncoder.encodeQueryComponent("\u0006"));
    assertEquals("%07", PercentEncoder.encodeQueryComponent("\u0007"));
    assertEquals("%08", PercentEncoder.encodeQueryComponent("\u0008"));
    assertEquals("%09", PercentEncoder.encodeQueryComponent("\u0009"));
    assertEquals("%0A", PercentEncoder.encodeQueryComponent("\n"));
    assertEquals("%0B", PercentEncoder.encodeQueryComponent("\u000b"));
    assertEquals("%0C", PercentEncoder.encodeQueryComponent("\u000c"));
    assertEquals("%0D", PercentEncoder.encodeQueryComponent("\r"));
    assertEquals("%0E", PercentEncoder.encodeQueryComponent("\u000e"));
    assertEquals("%0F", PercentEncoder.encodeQueryComponent("\u000f"));
    assertEquals("%10", PercentEncoder.encodeQueryComponent("\u0010"));
    assertEquals("%11", PercentEncoder.encodeQueryComponent("\u0011"));
    assertEquals("%12", PercentEncoder.encodeQueryComponent("\u0012"));
    assertEquals("%13", PercentEncoder.encodeQueryComponent("\u0013"));
    assertEquals("%14", PercentEncoder.encodeQueryComponent("\u0014"));
    assertEquals("%15", PercentEncoder.encodeQueryComponent("\u0015"));
    assertEquals("%16", PercentEncoder.encodeQueryComponent("\u0016"));
    assertEquals("%17", PercentEncoder.encodeQueryComponent("\u0017"));
    assertEquals("%18", PercentEncoder.encodeQueryComponent("\u0018"));
    assertEquals("%19", PercentEncoder.encodeQueryComponent("\u0019"));
    assertEquals("%1A", PercentEncoder.encodeQueryComponent("\u001a"));
    assertEquals("%1B", PercentEncoder.encodeQueryComponent("\u001b"));
    assertEquals("%1C", PercentEncoder.encodeQueryComponent("\u001c"));
    assertEquals("%1D", PercentEncoder.encodeQueryComponent("\u001d"));
    assertEquals("%1E", PercentEncoder.encodeQueryComponent("\u001e"));
    assertEquals("%1F", PercentEncoder.encodeQueryComponent("\u001f"));
    assertEquals("+", PercentEncoder.encodeQueryComponent("\u0020"));
    assertEquals("%22", PercentEncoder.encodeQueryComponent("\""));
    assertEquals("%23", PercentEncoder.encodeQueryComponent("#"));
    assertEquals("%25", PercentEncoder.encodeQueryComponent("%"));
    assertEquals("%3C", PercentEncoder.encodeQueryComponent("<"));
    assertEquals("%3E", PercentEncoder.encodeQueryComponent(">"));
    assertEquals("%5B", PercentEncoder.encodeQueryComponent("["));
    assertEquals("%5C", PercentEncoder.encodeQueryComponent("\\"));
    assertEquals("%5D", PercentEncoder.encodeQueryComponent("]"));
    assertEquals("%5E", PercentEncoder.encodeQueryComponent("^"));
    assertEquals("%60", PercentEncoder.encodeQueryComponent("`"));
    assertEquals("%7B", PercentEncoder.encodeQueryComponent("{"));
    assertEquals("%7C", PercentEncoder.encodeQueryComponent("|"));
    assertEquals("%7D", PercentEncoder.encodeQueryComponent("}"));
    assertEquals("%7F", PercentEncoder.encodeQueryComponent("\u007f"));
  }

  @Test public void rfc3986_queryEncodingOptional() {
    assertMaybeEncoded('!', PercentEncoder.encodeQueryComponent("!"));
    assertMaybeEncoded('$', PercentEncoder.encodeQueryComponent("$"));
    assertMaybeEncoded('&', PercentEncoder.encodeQueryComponent("&"));
    assertMaybeEncoded('\'', PercentEncoder.encodeQueryComponent("'"));
    assertMaybeEncoded('(', PercentEncoder.encodeQueryComponent("("));
    assertMaybeEncoded(')', PercentEncoder.encodeQueryComponent(")"));
    assertMaybeEncoded('*', PercentEncoder.encodeQueryComponent("*"));
    assertMaybeEncoded('+', PercentEncoder.encodeQueryComponent("+"));
    assertMaybeEncoded(',', PercentEncoder.encodeQueryComponent(","));
    assertMaybeEncoded('/', PercentEncoder.encodeQueryComponent("/"));
    assertMaybeEncoded(':', PercentEncoder.encodeQueryComponent(":"));
    assertMaybeEncoded(';', PercentEncoder.encodeQueryComponent(";"));
    assertMaybeEncoded('=', PercentEncoder.encodeQueryComponent("="));
    assertMaybeEncoded('?', PercentEncoder.encodeQueryComponent("?"));
    assertMaybeEncoded('@', PercentEncoder.encodeQueryComponent("@"));
  }

  @Test public void rfc3986_queryEncodingNotRecommended() {
    assertEquals("-", PercentEncoder.encodeQueryComponent("-"));
    assertEquals(".", PercentEncoder.encodeQueryComponent("."));
    assertEquals("_", PercentEncoder.encodeQueryComponent("_"));
    // This one is a quirk of form encoding where the tilde does get encoded.
    // Encoding an unreserved character is allowed by RFC 3986, although
    // not encouraged.
    assertMaybeEncoded('~', PercentEncoder.encodeQueryComponent("~"));

    assertEquals("0", PercentEncoder.encodeQueryComponent("0"));
    assertEquals("1", PercentEncoder.encodeQueryComponent("1"));
    assertEquals("2", PercentEncoder.encodeQueryComponent("2"));
    assertEquals("3", PercentEncoder.encodeQueryComponent("3"));
    assertEquals("4", PercentEncoder.encodeQueryComponent("4"));
    assertEquals("5", PercentEncoder.encodeQueryComponent("5"));
    assertEquals("6", PercentEncoder.encodeQueryComponent("6"));
    assertEquals("7", PercentEncoder.encodeQueryComponent("7"));
    assertEquals("8", PercentEncoder.encodeQueryComponent("8"));
    assertEquals("9", PercentEncoder.encodeQueryComponent("9"));
    assertEquals("A", PercentEncoder.encodeQueryComponent("A"));
    assertEquals("B", PercentEncoder.encodeQueryComponent("B"));
    assertEquals("C", PercentEncoder.encodeQueryComponent("C"));
    assertEquals("D", PercentEncoder.encodeQueryComponent("D"));
    assertEquals("E", PercentEncoder.encodeQueryComponent("E"));
    assertEquals("F", PercentEncoder.encodeQueryComponent("F"));
    assertEquals("G", PercentEncoder.encodeQueryComponent("G"));
    assertEquals("H", PercentEncoder.encodeQueryComponent("H"));
    assertEquals("I", PercentEncoder.encodeQueryComponent("I"));
    assertEquals("J", PercentEncoder.encodeQueryComponent("J"));
    assertEquals("K", PercentEncoder.encodeQueryComponent("K"));
    assertEquals("L", PercentEncoder.encodeQueryComponent("L"));
    assertEquals("M", PercentEncoder.encodeQueryComponent("M"));
    assertEquals("N", PercentEncoder.encodeQueryComponent("N"));
    assertEquals("O", PercentEncoder.encodeQueryComponent("O"));
    assertEquals("P", PercentEncoder.encodeQueryComponent("P"));
    assertEquals("Q", PercentEncoder.encodeQueryComponent("Q"));
    assertEquals("R", PercentEncoder.encodeQueryComponent("R"));
    assertEquals("S", PercentEncoder.encodeQueryComponent("S"));
    assertEquals("T", PercentEncoder.encodeQueryComponent("T"));
    assertEquals("U", PercentEncoder.encodeQueryComponent("U"));
    assertEquals("V", PercentEncoder.encodeQueryComponent("V"));
    assertEquals("W", PercentEncoder.encodeQueryComponent("W"));
    assertEquals("X", PercentEncoder.encodeQueryComponent("X"));
    assertEquals("Y", PercentEncoder.encodeQueryComponent("Y"));
    assertEquals("Z", PercentEncoder.encodeQueryComponent("Z"));
    assertEquals("a", PercentEncoder.encodeQueryComponent("a"));
    assertEquals("b", PercentEncoder.encodeQueryComponent("b"));
    assertEquals("c", PercentEncoder.encodeQueryComponent("c"));
    assertEquals("d", PercentEncoder.encodeQueryComponent("d"));
    assertEquals("e", PercentEncoder.encodeQueryComponent("e"));
    assertEquals("f", PercentEncoder.encodeQueryComponent("f"));
    assertEquals("g", PercentEncoder.encodeQueryComponent("g"));
    assertEquals("h", PercentEncoder.encodeQueryComponent("h"));
    assertEquals("i", PercentEncoder.encodeQueryComponent("i"));
    assertEquals("j", PercentEncoder.encodeQueryComponent("j"));
    assertEquals("k", PercentEncoder.encodeQueryComponent("k"));
    assertEquals("l", PercentEncoder.encodeQueryComponent("l"));
    assertEquals("m", PercentEncoder.encodeQueryComponent("m"));
    assertEquals("n", PercentEncoder.encodeQueryComponent("n"));
    assertEquals("o", PercentEncoder.encodeQueryComponent("o"));
    assertEquals("p", PercentEncoder.encodeQueryComponent("p"));
    assertEquals("q", PercentEncoder.encodeQueryComponent("q"));
    assertEquals("r", PercentEncoder.encodeQueryComponent("r"));
    assertEquals("s", PercentEncoder.encodeQueryComponent("s"));
    assertEquals("t", PercentEncoder.encodeQueryComponent("t"));
    assertEquals("u", PercentEncoder.encodeQueryComponent("u"));
    assertEquals("v", PercentEncoder.encodeQueryComponent("v"));
    assertEquals("w", PercentEncoder.encodeQueryComponent("w"));
    assertEquals("x", PercentEncoder.encodeQueryComponent("x"));
    assertEquals("y", PercentEncoder.encodeQueryComponent("y"));
    assertEquals("z", PercentEncoder.encodeQueryComponent("z"));
  }

  @Test public void rfc3986_fragmentEncodingRequired() {
    assertEquals("%00", PercentEncoder.encodeFragment("\u0000"));
    assertEquals("%01", PercentEncoder.encodeFragment("\u0001"));
    assertEquals("%02", PercentEncoder.encodeFragment("\u0002"));
    assertEquals("%03", PercentEncoder.encodeFragment("\u0003"));
    assertEquals("%04", PercentEncoder.encodeFragment("\u0004"));
    assertEquals("%05", PercentEncoder.encodeFragment("\u0005"));
    assertEquals("%06", PercentEncoder.encodeFragment("\u0006"));
    assertEquals("%07", PercentEncoder.encodeFragment("\u0007"));
    assertEquals("%08", PercentEncoder.encodeFragment("\u0008"));
    assertEquals("%09", PercentEncoder.encodeFragment("\u0009"));
    assertEquals("%0A", PercentEncoder.encodeFragment("\n"));
    assertEquals("%0B", PercentEncoder.encodeFragment("\u000b"));
    assertEquals("%0C", PercentEncoder.encodeFragment("\u000c"));
    assertEquals("%0D", PercentEncoder.encodeFragment("\r"));
    assertEquals("%0E", PercentEncoder.encodeFragment("\u000e"));
    assertEquals("%0F", PercentEncoder.encodeFragment("\u000f"));
    assertEquals("%10", PercentEncoder.encodeFragment("\u0010"));
    assertEquals("%11", PercentEncoder.encodeFragment("\u0011"));
    assertEquals("%12", PercentEncoder.encodeFragment("\u0012"));
    assertEquals("%13", PercentEncoder.encodeFragment("\u0013"));
    assertEquals("%14", PercentEncoder.encodeFragment("\u0014"));
    assertEquals("%15", PercentEncoder.encodeFragment("\u0015"));
    assertEquals("%16", PercentEncoder.encodeFragment("\u0016"));
    assertEquals("%17", PercentEncoder.encodeFragment("\u0017"));
    assertEquals("%18", PercentEncoder.encodeFragment("\u0018"));
    assertEquals("%19", PercentEncoder.encodeFragment("\u0019"));
    assertEquals("%1A", PercentEncoder.encodeFragment("\u001a"));
    assertEquals("%1B", PercentEncoder.encodeFragment("\u001b"));
    assertEquals("%1C", PercentEncoder.encodeFragment("\u001c"));
    assertEquals("%1D", PercentEncoder.encodeFragment("\u001d"));
    assertEquals("%1E", PercentEncoder.encodeFragment("\u001e"));
    assertEquals("%1F", PercentEncoder.encodeFragment("\u001f"));
    assertEquals("%20", PercentEncoder.encodeFragment("\u0020"));
    assertEquals("%22", PercentEncoder.encodeFragment("\""));
    assertEquals("%23", PercentEncoder.encodeFragment("#"));
    assertEquals("%25", PercentEncoder.encodeFragment("%"));
    assertEquals("%3C", PercentEncoder.encodeFragment("<"));
    assertEquals("%3E", PercentEncoder.encodeFragment(">"));
    assertEquals("%5B", PercentEncoder.encodeFragment("["));
    assertEquals("%5C", PercentEncoder.encodeFragment("\\"));
    assertEquals("%5D", PercentEncoder.encodeFragment("]"));
    assertEquals("%5E", PercentEncoder.encodeFragment("^"));
    assertEquals("%60", PercentEncoder.encodeFragment("`"));
    assertEquals("%7B", PercentEncoder.encodeFragment("{"));
    assertEquals("%7C", PercentEncoder.encodeFragment("|"));
    assertEquals("%7D", PercentEncoder.encodeFragment("}"));
    assertEquals("%7F", PercentEncoder.encodeFragment("\u007f"));
  }

  @Test public void rfc3986_fragmentEncodingOptional() {
    assertMaybeEncoded('!', PercentEncoder.encodeFragment("!"));
    assertMaybeEncoded('$', PercentEncoder.encodeFragment("$"));
    assertMaybeEncoded('&', PercentEncoder.encodeFragment("&"));
    assertMaybeEncoded('\'', PercentEncoder.encodeFragment("'"));
    assertMaybeEncoded('(', PercentEncoder.encodeFragment("("));
    assertMaybeEncoded(')', PercentEncoder.encodeFragment(")"));
    assertMaybeEncoded('*', PercentEncoder.encodeFragment("*"));
    assertMaybeEncoded('+', PercentEncoder.encodeFragment("+"));
    assertMaybeEncoded(',', PercentEncoder.encodeFragment(","));
    assertMaybeEncoded('/', PercentEncoder.encodeFragment("/"));
    assertMaybeEncoded('0', PercentEncoder.encodeFragment("0"));
    assertMaybeEncoded('1', PercentEncoder.encodeFragment("1"));
    assertMaybeEncoded('2', PercentEncoder.encodeFragment("2"));
    assertMaybeEncoded('3', PercentEncoder.encodeFragment("3"));
    assertMaybeEncoded('4', PercentEncoder.encodeFragment("4"));
    assertMaybeEncoded('5', PercentEncoder.encodeFragment("5"));
    assertMaybeEncoded('6', PercentEncoder.encodeFragment("6"));
    assertMaybeEncoded('7', PercentEncoder.encodeFragment("7"));
    assertMaybeEncoded('8', PercentEncoder.encodeFragment("8"));
    assertMaybeEncoded('9', PercentEncoder.encodeFragment("9"));
    assertMaybeEncoded(':', PercentEncoder.encodeFragment(":"));
    assertMaybeEncoded(';', PercentEncoder.encodeFragment(";"));
    assertMaybeEncoded('=', PercentEncoder.encodeFragment("="));
    assertMaybeEncoded('?', PercentEncoder.encodeFragment("?"));
    assertMaybeEncoded('@', PercentEncoder.encodeFragment("@"));
  }

  @Test public void rfc3986_fragmentEncodingNotRecommended() {
    assertEquals("-", PercentEncoder.encodeFragment("-"));
    assertEquals(".", PercentEncoder.encodeFragment("."));
    assertEquals("_", PercentEncoder.encodeFragment("_"));
    assertEquals("~", PercentEncoder.encodeFragment("~"));

    assertEquals("0", PercentEncoder.encodeFragment("0"));
    assertEquals("1", PercentEncoder.encodeFragment("1"));
    assertEquals("2", PercentEncoder.encodeFragment("2"));
    assertEquals("3", PercentEncoder.encodeFragment("3"));
    assertEquals("4", PercentEncoder.encodeFragment("4"));
    assertEquals("5", PercentEncoder.encodeFragment("5"));
    assertEquals("6", PercentEncoder.encodeFragment("6"));
    assertEquals("7", PercentEncoder.encodeFragment("7"));
    assertEquals("8", PercentEncoder.encodeFragment("8"));
    assertEquals("9", PercentEncoder.encodeFragment("9"));
    assertEquals("A", PercentEncoder.encodeFragment("A"));
    assertEquals("B", PercentEncoder.encodeFragment("B"));
    assertEquals("C", PercentEncoder.encodeFragment("C"));
    assertEquals("D", PercentEncoder.encodeFragment("D"));
    assertEquals("E", PercentEncoder.encodeFragment("E"));
    assertEquals("F", PercentEncoder.encodeFragment("F"));
    assertEquals("G", PercentEncoder.encodeFragment("G"));
    assertEquals("H", PercentEncoder.encodeFragment("H"));
    assertEquals("I", PercentEncoder.encodeFragment("I"));
    assertEquals("J", PercentEncoder.encodeFragment("J"));
    assertEquals("K", PercentEncoder.encodeFragment("K"));
    assertEquals("L", PercentEncoder.encodeFragment("L"));
    assertEquals("M", PercentEncoder.encodeFragment("M"));
    assertEquals("N", PercentEncoder.encodeFragment("N"));
    assertEquals("O", PercentEncoder.encodeFragment("O"));
    assertEquals("P", PercentEncoder.encodeFragment("P"));
    assertEquals("Q", PercentEncoder.encodeFragment("Q"));
    assertEquals("R", PercentEncoder.encodeFragment("R"));
    assertEquals("S", PercentEncoder.encodeFragment("S"));
    assertEquals("T", PercentEncoder.encodeFragment("T"));
    assertEquals("U", PercentEncoder.encodeFragment("U"));
    assertEquals("V", PercentEncoder.encodeFragment("V"));
    assertEquals("W", PercentEncoder.encodeFragment("W"));
    assertEquals("X", PercentEncoder.encodeFragment("X"));
    assertEquals("Y", PercentEncoder.encodeFragment("Y"));
    assertEquals("Z", PercentEncoder.encodeFragment("Z"));
    assertEquals("a", PercentEncoder.encodeFragment("a"));
    assertEquals("b", PercentEncoder.encodeFragment("b"));
    assertEquals("c", PercentEncoder.encodeFragment("c"));
    assertEquals("d", PercentEncoder.encodeFragment("d"));
    assertEquals("e", PercentEncoder.encodeFragment("e"));
    assertEquals("f", PercentEncoder.encodeFragment("f"));
    assertEquals("g", PercentEncoder.encodeFragment("g"));
    assertEquals("h", PercentEncoder.encodeFragment("h"));
    assertEquals("i", PercentEncoder.encodeFragment("i"));
    assertEquals("j", PercentEncoder.encodeFragment("j"));
    assertEquals("k", PercentEncoder.encodeFragment("k"));
    assertEquals("l", PercentEncoder.encodeFragment("l"));
    assertEquals("m", PercentEncoder.encodeFragment("m"));
    assertEquals("n", PercentEncoder.encodeFragment("n"));
    assertEquals("o", PercentEncoder.encodeFragment("o"));
    assertEquals("p", PercentEncoder.encodeFragment("p"));
    assertEquals("q", PercentEncoder.encodeFragment("q"));
    assertEquals("r", PercentEncoder.encodeFragment("r"));
    assertEquals("s", PercentEncoder.encodeFragment("s"));
    assertEquals("t", PercentEncoder.encodeFragment("t"));
    assertEquals("u", PercentEncoder.encodeFragment("u"));
    assertEquals("v", PercentEncoder.encodeFragment("v"));
    assertEquals("w", PercentEncoder.encodeFragment("w"));
    assertEquals("x", PercentEncoder.encodeFragment("x"));
    assertEquals("y", PercentEncoder.encodeFragment("y"));
    assertEquals("z", PercentEncoder.encodeFragment("z"));
  }

  private void assertMaybeEncoded(char codepoint, String encoded) {
    assertThat(encoded, anyOf(is("" + codepoint), is(encoded(codepoint))));
  }

  private String encoded(char c) {
    return String.format("%%%02X", (int) c);
  }
}