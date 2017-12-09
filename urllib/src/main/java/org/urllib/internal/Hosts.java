package org.urllib.internal;

import java.net.IDN;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.urllib.Host;

public final class Hosts {

  private static final Pattern EMPTY_SEGMENT = Pattern.compile("^\\.|\\.{2}");

  private static final CodepointMatcher DNS_OR_IPV6 = CodepointMatcher.or(
      CodepointMatcher.ALPHANUMERIC, CodepointMatcher.anyOf("-.[]:%"));

  @Nonnull public static Host parse(String hostname) {
    String ascii = validateAndConvertToAscii(hostname);

    Host host;
    if (Ip6.isIpv6(ascii)) {
      host = Ip6.parse(ascii);
    } else {
      if (ascii.endsWith(".")) {
        ascii = ascii.substring(0, ascii.length() - 1);
      }
      if (Ip4.isIpv4(ascii)) {
        host = Ip4.parse(ascii);
      } else {
        host = Dns.parse(ascii);
      }
    }

    if (host == null) {
      throw new IllegalArgumentException("Invalid hostname: " + hostname);
    }

    return host;
  }

  private static String validateAndConvertToAscii(String hostname) {
    if (Strings.isNullOrEmpty(hostname)
        || EMPTY_SEGMENT.matcher(hostname).find()) {
      throw new IllegalArgumentException("Invalid hostname: " + hostname);
    }

    for (int i = 0; i < hostname.length(); i++) {
      char c = hostname.charAt(i);
      if (c < 0x80 && !DNS_OR_IPV6.matches(c)) {
        throw new IllegalArgumentException("Invalid hostname: Illegal char at index " + i);
      }
    }

    String ascii;
    try {
      ascii = IDN.toASCII(PercentDecoder.decodeUnreserved(hostname), IDN.ALLOW_UNASSIGNED);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid hostname: " + hostname);
    }

    if (".".equals(ascii)) {
      throw new IllegalArgumentException("Invalid hostname: cannot be null or empty.");
    }

    return ascii;
  }

}
