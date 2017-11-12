package org.urllib.internal;

public abstract class PercentEncoder {

  private static final CodepointMatcher safePath =
      CodepointMatcher.anyOf(EncodeRules.PATH);

  private static final CodepointMatcher reEncodePath =
      CodepointMatcher.anyOf(EncodeRules.PATH + "\\/;");

  private static final CodepointMatcher reEncodeQuery =
      CodepointMatcher.anyOf(EncodeRules.QUERY + "=?&+;");

  private static final CodepointMatcher safeQuery =
      CodepointMatcher.anyOf(EncodeRules.QUERY);

  private static final CodepointMatcher safeFragment =
      CodepointMatcher.anyOf(EncodeRules.FRAGMENT);

  public static String encodePathSegment(String segment) {
    return PercentEncoder.encode(segment, safePath, false, false);
  }

  public static String encodeQueryComponent(String component) {
    return PercentEncoder.encode(component, safeQuery, true, false);
  }

  public static String encodeQueryComponentNoPlusForSpace(String component) {
    return PercentEncoder.encode(component, safeQuery, false, false);
  }

  public static String encodeFragment(String fragment) {
    return PercentEncoder.encode(fragment, safeFragment, false, false);
  }

  public static String reEncodePath(String path) {
    return PercentEncoder.encode(path, reEncodePath, false, true);
  }

  public static String reEncodeQuery(String query) {
    return PercentEncoder.encode(query, reEncodeQuery, false, true);
  }

  public static String reEncodeFragment(String fragment) {
    return PercentEncoder.encode(fragment, safeFragment, false, true);
  }

  private static final byte[] UPPER_HEX_DIGITS =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  private static String encode(String src, CodepointMatcher safe, boolean spaceToPlus,
      boolean reEncode) {
    if (allSafe(src, safe)) {
      return src;
    }
    int p = 0;
    int[] codepoints = Strings.codePoints(src);
    int[] dest = new int[maxEncodedSize(codepoints, safe, reEncode)];
    for (int i = 0, len = codepoints.length; i < len; i++) {
      int codepoint = codepoints[i];
      if (reEncode && codepoint == '%') {
        if (i < len - 2 && Hex.isHex(codepoints[i + 1]) && Hex.isHex(codepoints[i + 2])) {
          dest[p++] = '%';
        } else {
          dest[p++] = '%';
          dest[p++] = '2';
          dest[p++] = '5';
        }
      } else if (spaceToPlus && codepoint == ' ') {
        dest[p++] = '+';
      } else if (!safe.matches(codepoint)) {
        p += encodeTo(codepoint, p, dest);
      } else {
        dest[p++] = codepoint;
      }
    }
    return new String(dest, 0, p);
  }

  private static boolean allSafe(String src, CodepointMatcher safe) {
    for (int i = 0; i < src.length(); i++) {
      if (!safe.matches(src.charAt(i))) return false;
    }
    return true;
  }

  // From Guava's PercentEscaper.
  private static int encodeTo(int codepoint, int p, int[] dest) {
    if (codepoint <= 0x7F) {
      // Single byte UTF-8 characters
      // Start with "%--" and fill in the blanks
      dest[p] = '%';
      dest[p + 2] = UPPER_HEX_DIGITS[codepoint & 0xF];
      dest[p + 1] = UPPER_HEX_DIGITS[codepoint >>> 4];
      return 3;
    } else if (codepoint <= 0x7ff) {
      // Two byte UTF-8 characters [cp >= 0x80 && cp <= 0x7ff]
      // Start with "%--%--" and fill in the blanks
      dest[p] = '%';
      dest[p + 3] = '%';
      dest[p + 5] = UPPER_HEX_DIGITS[codepoint & 0xF];
      codepoint >>>= 4;
      dest[p + 4] = UPPER_HEX_DIGITS[0x8 | (codepoint & 0x3)];
      codepoint >>>= 2;
      dest[p + 2] = UPPER_HEX_DIGITS[codepoint & 0xF];
      codepoint >>>= 4;
      dest[p + 1] = UPPER_HEX_DIGITS[0xC | codepoint];
      return 6;
    } else if (codepoint <= 0xffff) {
      // Three byte UTF-8 characters [cp >= 0x800 && cp <= 0xffff]
      // Start with "%E-%--%--" and fill in the blanks
      dest[p] = '%';
      dest[p + 1] = 'E';
      dest[p + 3] = '%';
      dest[p + 6] = '%';
      dest[p + 8] = UPPER_HEX_DIGITS[codepoint & 0xF];
      codepoint >>>= 4;
      dest[p + 7] = UPPER_HEX_DIGITS[0x8 | (codepoint & 0x3)];
      codepoint >>>= 2;
      dest[p + 5] = UPPER_HEX_DIGITS[codepoint & 0xF];
      codepoint >>>= 4;
      dest[p + 4] = UPPER_HEX_DIGITS[0x8 | (codepoint & 0x3)];
      codepoint >>>= 2;
      dest[p + 2] = UPPER_HEX_DIGITS[codepoint];
      return 9;
    } else if (codepoint <= 0x10ffff) {
      // Four byte UTF-8 characters [cp >= 0xffff && cp <= 0x10ffff]
      // Start with "%F-%--%--%--" and fill in the blanks
      dest[p] = '%';
      dest[p + 1] = 'F';
      dest[p + 3] = '%';
      dest[p + 6] = '%';
      dest[p + 9] = '%';
      dest[p + 11] = UPPER_HEX_DIGITS[codepoint & 0xF];
      codepoint >>>= 4;
      dest[p + 10] = UPPER_HEX_DIGITS[0x8 | (codepoint & 0x3)];
      codepoint >>>= 2;
      dest[p + 8] = UPPER_HEX_DIGITS[codepoint & 0xF];
      codepoint >>>= 4;
      dest[p + 7] = UPPER_HEX_DIGITS[0x8 | (codepoint & 0x3)];
      codepoint >>>= 2;
      dest[p + 5] = UPPER_HEX_DIGITS[codepoint & 0xF];
      codepoint >>>= 4;
      dest[p + 4] = UPPER_HEX_DIGITS[0x8 | (codepoint & 0x3)];
      codepoint >>>= 2;
      dest[p + 2] = UPPER_HEX_DIGITS[codepoint & 0x7];
      return 12;
    } else {
      throw new IllegalArgumentException("Invalid unicode character value " + codepoint);
    }
  }

  private static int maxEncodedSize(int[] codepoints, CodepointMatcher safe, boolean reEncode) {
    int size = 0;
    for (int codepoint : codepoints) {
      if (safe.matches(codepoint)) {
        size++;
      } else if (codepoint <= 0x7F) {
        size += 3;
      } else if (codepoint <= 0x7ff) {
        size += 6;
      } else if (codepoint <= 0xffff) {
        size += 9;
      } else if (codepoint <= 0x10ffff) {
        size += 12;
      } else {
        throw new IllegalArgumentException("Invalid unicode character value " + codepoint);
      }
    }
    return size;
  }

}
