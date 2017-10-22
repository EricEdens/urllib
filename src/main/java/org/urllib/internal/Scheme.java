package org.urllib.internal;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Scheme {

  public static final Scheme HTTP = of("http", 80);
  public static final Scheme HTTPS = of("https", 443);

  public abstract String name();
  public abstract int defaultPort();

  private static Scheme of(String name, int defaultPort) {
    return new AutoValue_Scheme(name, defaultPort);
  }

}
