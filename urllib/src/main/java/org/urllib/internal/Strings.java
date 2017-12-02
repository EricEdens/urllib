package org.urllib.internal;

import java.util.Arrays;

public final class Strings {

  private Strings() {}

  public static int[] codePoints(String s) {
    int length = s.length();
    int arrayPointer = 0;
    int stringPointer = 0;
    int[] codepoints = new int[length];

    while (stringPointer < length) {
      int codepoint = s.codePointAt(stringPointer);
      codepoints[arrayPointer++] = codepoint;
      stringPointer += Character.charCount(codepoint);
    }

    return arrayPointer == stringPointer
        ? codepoints
        : Arrays.copyOf(codepoints, arrayPointer);
  }

  public static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }

  public static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }


  public static String sanitizeWhitespace(String str) {

    int start = 0;
    int end = str.length();

    while (start < end && CodepointMatcher.ASCII_WHITESPACE.matches(str.charAt(start))) {
      start++;
    }

    if (start == end) {
      return "";
    }

    while (end > start && CodepointMatcher.ASCII_WHITESPACE.matches(str.charAt(end - 1))) {
      end--;
    }

    int firstNewline = -1;

    for (int i = start; i < end; i++) {
      if (CodepointMatcher.ASCII_NEWLINE.matches(str.charAt(i))) {
        firstNewline = i;
        break;
      }
    }

    if (firstNewline == -1) {
      if (start == 0 && end == str.length()) {
        return str;
      } else {
        return str.substring(start, end);
      }
    }

    char[] chars = str.toCharArray();

    int p = firstNewline;
    for (int i = firstNewline; i < end; i++) {
      if (CodepointMatcher.ASCII_NEWLINE.matches(str.charAt(i))) {
        i++;
        while (i < end && CodepointMatcher.ASCII_WHITESPACE.matches(str.charAt(i))) {
          i++;
        }
      }
      chars[p++] = str.charAt(i);
    }

    return new String(chars, start, p - start);
  }

}
