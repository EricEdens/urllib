package org.urllib.internal.authority;

import com.google.auto.value.AutoValue;
import java.net.IDN;
import java.util.Locale;
import org.urllib.Host;
import org.urllib.internal.CodepointMatcher;

@AutoValue
abstract class Dns implements Host {

  private static final CodepointMatcher DNS = CodepointMatcher.or(
      CodepointMatcher.ALPHANUMERIC, CodepointMatcher.anyOf("-."));

  static Dns parse(String hostname) {
    int lastDot = -1;
    for (int i = 0; i < hostname.length(); i++) {
      char c = hostname.charAt(i);
      if (!DNS.matches(c)) {
        throw new InvalidHostException(hostname, i);
      } else if (c == '.') {
        if (lastDot == i - 1) {
          throw new InvalidHostException(hostname, i);
        }
        lastDot = i;
      }
    }
    String lower = hostname.toLowerCase(Locale.US);
    return new AutoValue_Dns(lower, IDN.toUnicode(lower));
  }
}
