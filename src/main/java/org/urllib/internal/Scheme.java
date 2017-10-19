package org.urllib.internal;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Scheme {

  public static final Scheme HTTP = new AutoValue_Scheme("http", 80);
  public static final Scheme HTTPS = new AutoValue_Scheme("https", 443);

  public abstract String name();
  public abstract int defaultPort();
}
