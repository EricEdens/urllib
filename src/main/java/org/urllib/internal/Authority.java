package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.urllib.UrlException;

@AutoValue
public abstract class Authority {

  // https://tools.ietf.org/html/rfc4366#section-3.1
  private static final CodepointMatcher DOTS_MATCHER =
      CodepointMatcher.anyOf(".\u3002\uFF0E\uFF61");

  private static final CodepointMatcher DNS_SEGMENT_MATCHER =
      CodepointMatcher.or(CodepointMatcher.ALPHANUMERIC, CodepointMatcher.anyOf("-"));

  public abstract int port();
  public abstract String host();
  public abstract List<String> hostSegments();

  public static Authority split(String authority) {
    int lastColon = -1;
    int numColons = 0;

    int[] points = Strings.codePoints(authority);
    int p = points.length;
    int end = points.length;
    int port = -1;

    // Move p to the last @, or -1 if not found
    // Find the last colon
    // Count the number of colons found
    while (--p >= 0) {
      int b = points[p];
      if (b == '@') {
        break;
      } else if (b == ':') {
        if (numColons++ == 0) {
          lastColon = p;
        }
      }
    }

    // Move p to the first byte in the authority
    p++;

    if (p == end || p == lastColon) {
      throw new UrlException("Host cannot be empty. Input: " + authority);
    }

    if (numColons == 1) {
      port = parseAndValidatePort(points, lastColon);
      end = lastColon;
    } else if (numColons > 1) {
      if (points[lastColon - 1] == ']') {
        port = parseAndValidatePort(points, lastColon);
        end = lastColon;
      }
      return ipv6(points, p, end, port);
    }

    return dnsOrIpv4(points, p, end, port);
  }

  private static Authority ipv6(int[] points, int start, int end, int port) {
    throw new UnsupportedOperationException("Ipv6 addresses not supported yet.");
  }

  private static Authority dnsOrIpv4(int[] points, int start, int end, int port) {

    List<String> segments = new ArrayList<>();
    boolean maybeIpv4 = true;
    int lastSegment = start;

    if (end > 0 && DOTS_MATCHER.matches(points[end - 1])) {
      end--;
    }

    for (int i = start; i < end; i++) {
      int b = points[i];
      if (DOTS_MATCHER.matches(b)) {
        if (lastSegment == i) {
          throw new UrlException("Two adjacent dots found in host name.");
        }
        segments.add(new String(points, lastSegment, i - lastSegment));
        lastSegment = i + 1;
      } else if (!DNS_SEGMENT_MATCHER.matches(b)) {
        throw new UrlException(String.format("Invalid byte in host name: 0x%X", (int) b));
      } else if (!CodepointMatcher.DIGIT.matches(b)) {
        maybeIpv4 = false;
      }
    }

    if (lastSegment != end) {
      segments.add(new String(points, lastSegment, end - lastSegment));
    }

    String hostString;
    if (maybeIpv4) {
      hostString = validateAndFormatIpv4(segments);
    } else {
      hostString = validateAndFormatDns(segments);
    }

    return new AutoValue_Authority(port, hostString, segments);
  }

  private static String validateAndFormatIpv4(List<String> segments) {
    throw new UnsupportedOperationException("Ipv4 addresses not supported yet.");
  }

  private static String validateAndFormatDns(List<String> segments) {
    for (Iterator<String> iterator = segments.iterator(); iterator.hasNext(); ) {
      String segment = iterator.next();
      assert !segment.isEmpty() : "Internal bug: segments should not be empty. State=" + segment;
      if (!iterator.hasNext()) {
        if (CodepointMatcher.DIGIT.matches(segment.charAt(0))) {
          throw new UrlException("Last segment in host name cannot start with a digit.");
        }
      }
    }
    return Joiner.on('.').join(segments);
  }

  private static int parseAndValidatePort(int[] points, int lastColon) {

    String portString = new String(
        points, lastColon + 1, points.length - lastColon - 1);

    if (portString.isEmpty()) {
      return -1;
    }

    return Port.validateOrThrow(portString);
  }

  public static Authority of(String authority, int port, List<String> hostSegments) {
    return new AutoValue_Authority(port, authority, hostSegments);
  }
}
