package org.urllib.internal.authority;

import java.net.IDN;
import javax.annotation.Nonnull;
import org.urllib.Host;
import org.urllib.internal.PercentDecoder;

final class Hosts {

  @Nonnull static Host parse(String hostname) {
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
    String ascii;
    try {
      ascii = IDN.toASCII(PercentDecoder.decodeUnreserved(hostname), IDN.ALLOW_UNASSIGNED);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid hostname: " + hostname);
    }

    if (ascii.isEmpty() || ".".equals(ascii)) {
      throw new IllegalArgumentException("Invalid hostname: cannot be null or empty.");
    }

    return ascii;
  }

}
