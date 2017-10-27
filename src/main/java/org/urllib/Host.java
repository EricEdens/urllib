package org.urllib;

import java.net.IDN;
import java.util.Locale;
import java.util.regex.Pattern;
import org.urllib.internal.CodepointMatcher;
import org.urllib.internal.PercentDecoder;
import org.urllib.internal.Strings;

public abstract class Host {

  private static final Pattern EMPTY_SEGMENT = Pattern.compile("^\\.|\\.{2}");

  private static final CodepointMatcher DNS_OR_IPV6 = CodepointMatcher.or(
      CodepointMatcher.ALPHANUMERIC, CodepointMatcher.anyOf("-.[]:%"));

  private static final CodepointMatcher DNS = CodepointMatcher.or(
      CodepointMatcher.ALPHANUMERIC, CodepointMatcher.anyOf("-."));

  /**
   * Returns the Url's host, encoded so that it can be passed to methods such as
   * {@link java.net.InetAddress#getByName(String)}.
   *
   * <table>
   * <tr>
   * <th>Type</td>
   * <th>URL</td>
   * <th>Host.toString()</td>
   * <th>Host.name()</td>
   * </tr>
   * <tr>
   * <td>ASCII DNS</td>
   * <td>http://duckduckgo.com/</td>
   * <td>duckduckgo.com</td>
   * <td>duckduckgo.com</td>
   * </tr>
   * <tr>
   * <td>International</td>
   * <td>http://кот.ru/</td>
   * <td>кот.ru</td>
   * <td>xn--j1aim.ru</td>
   * </tr>
   * <tr>
   * <td>IPv4</td>
   * <td>http://10.20.30.40/</td>
   * <td>10.20.30.40</td>
   * <td>10.20.30.40</td>
   * </tr>
   * <tr>
   * <td>IPv6</td>
   * <td>http://[2001:db8::1:0:0:1]/</td>
   * <td>2001:db8::1:0:0:1</td>
   * <td>[2001:db8::1:0:0:1]</td>
   * </tr>
   * </table>
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2.2">RFC 3986#3.2.2</a>
   */
  public abstract String name();

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Host) {
      Host that = (Host) o;
      return this.name().equals(that.name());
    }
    return false;
  }

  @Override public int hashCode() {
    return name().hashCode();
  }

  public static Host forString(String hostname) {
    String ascii = validateAndConvertToAscii(hostname);

    Host host;
    if (isIpv6(ascii)) {
      host = Ip6.parse(ascii);
    } else {
      if (ascii.endsWith(".")) {
        ascii = ascii.substring(0, ascii.length() - 1);
      }
      if (isIpv4(ascii)) {
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
      ascii = IDN.toASCII(PercentDecoder.decode(hostname, DNS), IDN.ALLOW_UNASSIGNED);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid hostname: " + hostname);
    }

    if (".".equals(ascii)) {
      throw new IllegalArgumentException("Invalid hostname: cannot be null or empty.");
    }

    return ascii;
  }

  private static boolean isIpv4(String hostname) {
    int dot = hostname.lastIndexOf('.');
    if (dot == hostname.length() - 1) {
      return false;
    }
    // If a dot isn't found, then -1 is returned and we check the first character.
    return CodepointMatcher.DIGIT.matches(hostname.charAt(dot + 1));
  }

  private static boolean isIpv6(String hostname) {
    for (int i = 0; i < hostname.length(); i++) {
      switch (hostname.charAt(i)) {
        case '[':
        case ':':
          return true;
        case '.':
          return false;
      }
    }
    return false;
  }

  private static class Dns extends Host {

    private final String ascii;
    private final String display;

    Dns(String ascii, String display) {
      this.ascii = ascii;
      this.display = display;
    }

    static Dns parse(String hostname) {
      for (int i = 0; i < hostname.length(); i++) {
        if (!DNS.matches(hostname.charAt(i))) {
          return null;
        }
      }
      validate(hostname);
      String lower = hostname.toLowerCase(Locale.US);
      return new Dns(lower, IDN.toUnicode(lower));
    }

    private static void validate(String hostname) {

    }

    @Override public String name() {
      return ascii;
    }

    @Override public String toString() {
      return display;
    }
  }

  private static class Ip4 extends Host {

    private final String name;

    Ip4(String name) {
      this.name = name;
    }

    static Ip4 parse(String hostname) {
      String[] segments = hostname.split("\\.");
      if (segments.length != 4) {
        return null;
      }
      byte[] addr = new byte[4];
      for (int i = 0; i < segments.length; i++) {
        int val;
        String segment = segments[i];
        // Don't allow segments that start with zero, since
        // it's unclear whether they're octal.
        if (segment.length() > 1 && segment.startsWith("0")) {
          return null;
        }
        try {
          val = Integer.parseInt(segment);
        } catch (NumberFormatException e) {
          return null;
        }
        if (val < 0 || val > 255) {
          return null;
        }
        addr[i] = (byte) val;
      }
      return fromAddress(addr);
    }

    private static Ip4 fromAddress(byte[] addr) {
      return new Ip4((addr[0] & 0xff) + "." + (addr[1] & 0xff)
          + "." + (addr[2] & 0xff) + "." + (addr[3] & 0xff));
    }

    @Override public String name() {
      return name;
    }

    @Override public String toString() {
      return name;
    }
  }

  private static class Ip6 extends Host {

    // There are 16 bytes in Ipv6; we track
    // the address as 8 shorts.
    private static final int ADDRLEN = 8;

    private final String host;
    private final String hostWithBrackets;

    Ip6(String host) {
      this.host = host;
      this.hostWithBrackets = '[' + host + ']';
    }

    static Ip6 parse(String ip) {

      // 1. Trim brackets, if present.
      int start = 0;
      int end = ip.length();
      if (ip.startsWith("[")) {
        if (!ip.endsWith("]")) {
          return null;
        }
        start++;
        end--;
      }

      // 2. Short circuit for addresses that
      // are empty or too small.
      switch (end - start) {
        case 0:
        case 1:
          return null;
        case 2:
          return "::".equals(ip.substring(start, end))
              ? fromAddress(new int[8])
              : null;
        default:
      }

      int[] addr = new int[ADDRLEN];
      int addrPointer = 0;
      int compressionStarts = -1;

      // 3. Ensure that a leading colon means
      // there are exactly two colons, in
      // which case the address is compressed
      // at the front.
      if (ip.charAt(start) == ':') {
        if (ip.charAt(start + 1) != ':') {
          return null;
        }
        if (ip.charAt(start + 2) == ':') {
          return null;
        }
        compressionStarts = 0;
        start += 2;
      }

      // 4. Split at each segment,
      // interpreting the character
      // hex values.
      for (int i = start; i < end; i++) {
        if (addrPointer == ADDRLEN) {
          return null;
        }
        if (ip.charAt(i) == ':') {
          if (compressionStarts != -1) {
            return null;
          }
          compressionStarts = addrPointer;
          continue;
        }

        // 5. Decode the hex segment.
        int segEnd = Math.min(i + 4, end);
        int segVal = 0;
        for (; i < segEnd; i++) {
          char c = ip.charAt(i);
          if (c == ':') {
            break;
          }
          int hex = toHex(c);
          if (hex == -1) {
            return null;
          }
          segVal = segVal * 16 | hex;
        }
        addr[addrPointer++] = segVal;

        // 6. Ensure that the ip address
        // doesn't end in a colon.
        if (end == i) {
          break;
        } else if (ip.charAt(i) == ':') {
          // Don't allow trailing colon.
          if (i == end - 1) {
            return null;
          }
        } else {
          return null;
        }
      }

      // 7. Insert the compressed zeroes.
      if (compressionStarts == -1 && addrPointer < ADDRLEN - 1) {
        return null;
      } else if (compressionStarts > -1) {
        if (addrPointer == ADDRLEN) {
          return null;
        }
        for (int i = 1; i <= addrPointer - compressionStarts; i++) {
          addr[ADDRLEN - i] = addr[addrPointer - i];
          addr[addrPointer - i] = 0;
        }
      }

      return fromAddress(addr);
    }

    private static Ip6 fromAddress(int[] addr) {
      // 1. Find where to compress. Prefer compressing on right side,
      // and only compress if more than one segment can be eliminated.
      byte[] zeroesStartingHere = {
          0, 0, 0, 0, 0, 0, 0, (byte) (addr[7] == 0 ? 1 : 0)
      };
      int startCompress = ADDRLEN;
      byte numCompress = 0;
      for (int i = addr.length - 2; i >= 0; i--) {
        if (addr[i] == 0) {
          zeroesStartingHere[i] = (byte) (zeroesStartingHere[i + 1] + 1);
          if (zeroesStartingHere[i] > 1 && zeroesStartingHere[i] >= numCompress) {
            numCompress = zeroesStartingHere[i];
            startCompress = i;
          }
        }
      }

      int endCompress = startCompress == ADDRLEN
          ? ADDRLEN
          : startCompress + zeroesStartingHere[startCompress];

      StringBuilder sb = new StringBuilder();
      if (startCompress == 0) {
        sb.append(':');
      }
      for (int i = 0; i < ADDRLEN; i++) {
        if (i == startCompress) {
          sb.append(':');
          continue;
        } else if (i > startCompress && i < endCompress) {
          continue;
        }
        sb.append(Integer.toHexString(addr[i]));
        if (i < addr.length - 1) {
          sb.append(':');
        }
      }
      return new Ip6(sb.toString());
    }

    private static int toHex(char c) {
      int hex;
      if ('0' <= c && c <= '9') {
        hex = c - '0';
      } else if ('a' <= c && c <= 'f') {
        hex = 10 + c - 'a';
      } else if ('A' <= c && c <= 'F') {
        hex = 10 + c - 'A';
      } else {
        hex = -1;
      }
      return hex;
    }

    @Override public String name() {
      return hostWithBrackets;
    }

    @Override public String toString() {
      return host;
    }
  }
}
