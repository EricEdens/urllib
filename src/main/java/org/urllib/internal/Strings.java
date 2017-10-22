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
}
