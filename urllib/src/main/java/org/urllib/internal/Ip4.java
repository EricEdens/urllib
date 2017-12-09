package org.urllib.internal;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.urllib.Host;

@AutoValue
abstract class Ip4 implements Host {

  @Nullable static Ip4 parse(String hostname) {
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
    String formatted = (addr[0] & 0xff) + "." + (addr[1] & 0xff)
        + "." + (addr[2] & 0xff) + "." + (addr[3] & 0xff);
    return new AutoValue_Ip4(formatted, formatted);
  }

  static boolean isIpv4(String hostname) {
    int dot = hostname.lastIndexOf('.');
    if (dot == hostname.length() - 1) {
      return false;
    }
    // If a dot isn't found, then -1 is returned and we check the first character.
    return CodepointMatcher.DIGIT.matches(hostname.charAt(dot + 1));
  }
}
