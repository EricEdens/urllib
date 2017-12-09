package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.net.IDN;
import java.util.Locale;
import org.urllib.Host;

@AutoValue
abstract class Dns implements Host {

  private static final CodepointMatcher DNS = CodepointMatcher.or(
      CodepointMatcher.ALPHANUMERIC, CodepointMatcher.anyOf("-."));


  public static Dns parse(String hostname) {
    for (int i = 0; i < hostname.length(); i++) {
      if (!DNS.matches(hostname.charAt(i))) {
        return null;
      }
    }
    String lower = hostname.toLowerCase(Locale.US);
    return create(lower, IDN.toUnicode(lower));
  }


  public static Dns create(String name, String display) {return new AutoValue_Dns(name, display);}
}
