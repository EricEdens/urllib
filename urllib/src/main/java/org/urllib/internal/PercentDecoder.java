package org.urllib.internal;

public final class PercentDecoder {

  private PercentDecoder() {}

  public static String decodeAll(String str) {
    return decode(str, CodepointMatcher.ALL);
  }

  public static String decodeUnreserved(String str) {
    return decode(str, CodepointMatcher.UNRESERVED);
  }

  private static String decode(String str, CodepointMatcher decodeSet) {
    if (str.isEmpty()) return str;
    if (!requiresDecoding(str, decodeSet)) return str;

    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    int p = 0;
    int i = 0;
    for (int end = bytes.length; i < end; i++) {
      byte b = bytes[i];
      if (b == '%' && i < end - 2) {
        byte b1 = bytes[i + 1];
        byte b2 = bytes[i + 2];
        int decoded = Hex.decodeHex(b1, b2);
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
    return new String(bytes, 0, p, StandardCharsets.UTF_8);
  }

  private static boolean requiresDecoding(String str, CodepointMatcher decodeSet) {
    for (int i = 0; i < str.length() - 2; i++) {
      char c = str.charAt(i);
      if (c == '%') {
        char c1 = str.charAt(i + 1);
        char c2 = str.charAt(i +2);
        int decoded = Hex.decodeHex(c1, c2);
        if (decoded != -1 && decodeSet.matches(decoded)) {
          return true;
        }
      }
    }
    return false;
  }
}
