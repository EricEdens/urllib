package org.urllib.internal;

public final class Hex {

  static int decodeHex(int b1, int b2) {
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
