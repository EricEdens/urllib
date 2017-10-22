package org.urllib.internal;

import java.nio.charset.Charset;

public final class StandardCharsets {

  private StandardCharsets() {}

  public static final Charset US_ASCII = Charset.forName("US-ASCII");
  public static final Charset UTF_8 = Charset.forName("UTF-8");
}