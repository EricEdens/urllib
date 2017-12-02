package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.Locale;

@AutoValue
public abstract class Scheme {

  public static final Scheme HTTP = create("http", 80);
  public static final Scheme HTTPS = create("https", 443);

  public abstract String name();
  public abstract int defaultPort();

  public static Scheme create(String name, int port) {return new AutoValue_Scheme(name, port);}
  public static Scheme valueOf(String scheme) {
    switch (scheme.toLowerCase(Locale.US)) {
      case "http":
        return HTTP;
      case "https":
        return HTTPS;
    }
    throw new IllegalArgumentException("Scheme must be http or https.");
  }
}
