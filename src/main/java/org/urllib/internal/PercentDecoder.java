package org.urllib.internal;

public class PercentDecoder {

  public static String decode(String str, CodepointMatcher decodeSet) {
    if (str.indexOf('%') < 0) return str;

    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    int p = 0;
    int i = 0;
    for (int end = bytes.length; i < end; i++) {
      byte b = bytes[i];
      if (b == '%' && i < end - 2) {
        byte b1 = bytes[i + 1];
        byte b2 = bytes[i + 2];
        int decoded = decodeHex(b1, b2);
        if (decoded > -1 && decodeSet.matches(decoded)) {
          bytes[p++] = (byte) decoded;
          i += 2;
          continue;
        }
      }

      if (p != i) {
        bytes[p] = b;
      }
      p++;
    }
    if (p == i) {
      return str;
    } else {
      return new String(bytes, 0, p, StandardCharsets.UTF_8);
    }
  }

  private static int decodeHex(int b1, int b2) {
    int i1 = decodeHex(b1);
    int i2 = decodeHex(b2);
    return i1 > -1 && i2 > -1
        ? i1 << 4 | i2
        : -1;
  }

  private static int decodeHex(int b) {
    if (b >= '0' && b <= '9') {
      return b - '0';
    } else if (b >= 'A' && b <= 'F') {
      return b - 'A' + 0xA;
    } else if (b >= 'a' && b <= 'f') {
      return b - 'a' + 0xA;
    } else {
      return -1;
    }
  }
}
