package org.urllib.internal;

import java.util.Arrays;
import javax.annotation.Nonnull;

public abstract class CodepointMatcher {

  public abstract boolean matches(int codepoint);

  public String trim(@Nonnull String str) {
    if (str.isEmpty()) return str;
    int start = 0;
    int end = str.length();
    while (start < end && matches(str.charAt(start))) {
      start++;
    }
    while (start < end && matches(str.charAt(end - 1))) {
      end--;
    }
    return start == 0 && end == str.length()
        ? str
        : str.substring(start, end);
  }

  public boolean matches(char c) {
    return matches((int) c);
  }

  public static final CodepointMatcher NONE = new CodepointMatcher() {
    @Override public boolean matches(int codepoint) {
      return false;
    }
  };

  public static final CodepointMatcher ALL = new CodepointMatcher() {
    @Override public boolean matches(int codepoint) {
      return true;
    }
  };

  public static final CodepointMatcher WHITESPACE = anyOf(
      0x0009,
      0x000A,
      0x000B,
      0x000C,
      0x000D,
      0x0020,
      0x0085,
      0x00A0,
      0x1680,
      0x2000,
      0x2001,
      0x2002,
      0x2003,
      0x2004,
      0x2005,
      0x2006,
      0x2007,
      0x2008,
      0x2009,
      0x200A,
      0x2028,
      0x2029,
      0x202F,
      0x205F,
      0x3000);

  public static final CodepointMatcher ALPHA = new CodepointMatcher() {
    @Override public boolean matches(int codepoint) {
      return codepoint >= 'a' && codepoint <= 'z'
          || codepoint >= 'A' && codepoint <= 'Z';
    }
  };

  public static final CodepointMatcher DIGIT = new CodepointMatcher() {
    @Override public boolean matches(int codepoint) {
      return codepoint >= '0' && codepoint <= '9';
    }
  };

  public static final CodepointMatcher ALPHANUMERIC = or(ALPHA, DIGIT);

  public static CodepointMatcher or(CodepointMatcher one, CodepointMatcher two) {
    return new CodepointMatcher() {
      @Override public boolean matches(int codepoint) {
        return one.matches(codepoint) || two.matches(codepoint);
      }
    };
  }

  public static final CodepointMatcher anyOf(String str) {
    return anyOf(str.codePoints().toArray());
  }

  public static final CodepointMatcher anyOf(int... codepoints) {
    Arrays.sort(codepoints);
    return new CodepointMatcher() {
      @Override public boolean matches(int codepoint) {
        return Arrays.binarySearch(codepoints, codepoint) > -1;
      }
    };
  }

  public static CodepointMatcher or(char c1, char c2) {
    return new CodepointMatcher() {
      @Override public boolean matches(int codepoint) {
        return codepoint == c1 || codepoint == c2;
      }
    };
  }

  public boolean matchesAnyOf(String str) {
    return str.codePoints().anyMatch(this::matches);
  }

}
