package org.urllib.internal.authority;

import com.google.auto.value.AutoValue;
import org.urllib.Host;

@AutoValue
public abstract class Authority {

  public abstract int port();
  public abstract Host host();

  public static Authority split(String authority) {
    int lastColon = -1;
    int numColons = 0;

    int start = authority.length();
    int end = authority.length();
    int port = -1;

    // Move p to the last @, or -1 if not found
    // Find the last colon
    // Count the number of colons found
    while (--start >= 0) {
      char b = authority.charAt(start);
      if (b == '@') {
        break;
      } else if (b == ':') {
        if (numColons++ == 0) {
          lastColon = start;
        }
      }
    }

    // Move p to the first character in the authority
    start++;

    if (start == end || start == lastColon) {
      throw new IllegalArgumentException("URL missing host. Input: " + authority);
    }

    if (numColons == 1) {
      port = parseAndValidatePort(authority, lastColon);
      end = lastColon;
    } else if (numColons > 1) {
      if (authority.charAt(lastColon - 1) == ']') {
        port = parseAndValidatePort(authority, lastColon);
        end = lastColon;
      }
    }

    return new AutoValue_Authority(port, Hosts.parse(authority.substring(start, end)));
  }

  @Override public String toString() {
    if (port() > 0) {
      return host().name() + ':' + port();
    } else {
      return host().name();
    }
  }

  private static int parseAndValidatePort(String authority, int lastColon) {
    return lastColon == authority.length() - 1
        ? -1
        : Port.validateOrThrow(authority.substring(lastColon + 1));
  }
}
