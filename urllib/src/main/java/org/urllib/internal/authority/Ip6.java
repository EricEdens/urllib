package org.urllib.internal.authority;

import com.google.auto.value.AutoValue;
import org.urllib.Host;

@AutoValue
abstract class Ip6 implements Host {

  // There are 16 bytes in Ipv6; we track
  // the address as 8 shorts.
  private static final int ADDRLEN = 8;

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
    String noBrackets = sb.toString();
    return new AutoValue_Ip6('[' + noBrackets + ']', noBrackets);
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

  static boolean isIpv6(String hostname) {
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
}
