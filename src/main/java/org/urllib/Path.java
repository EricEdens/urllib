package org.urllib;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.urllib.internal.CodepointMatcher;
import org.urllib.internal.Joiner;
import org.urllib.internal.PercentEncoder;

public final class Path {

  private static final Joiner DIRECTORY_JOINER = Joiner.on("/", "/", "/");
  private static final Joiner FILE_JOINER = Joiner.on("/", "/", "");
  private static final CodepointMatcher SLASH_MATCHER = CodepointMatcher.anyOf("/\\");
  private static final Pattern SINGLE_DOT = Pattern.compile("^\\.|%2[eE]$");
  private static final Pattern DOUBLE_DOT = Pattern.compile("^(\\.|%2[eE]){2}$");
  private static final Path empty = of(Collections.<String>emptyList(), true);

  @Nonnull private final List<String> segments;
  private final boolean isDir;
  @Nonnull private final String encoded;

  Path(@Nonnull List<String> segments, boolean isDir, @Nonnull String encoded) {
    this.segments = segments;
    this.isDir = isDir;
    this.encoded = encoded;
  }

  static Path empty() {
    return empty;
  }

  static Path of(String... segments) {
    if (segments.length == 0) {
      return empty;
    }

    PathSegments pathSegments = new PathSegments();

    for (String segment : segments) {
      if (SLASH_MATCHER.matchesAnyOf(segment)) {
        int i = 0;
        int j = 0;
        for (; j < segment.length(); j++) {
          if (SLASH_MATCHER.matches(segment.charAt(j))) {
            pathSegments.add(segment.substring(i, j));
            i = j + 1;
          }
        }
        pathSegments.add(segment.substring(i, j));
      } else {
        pathSegments.add(segment);
      }
    }

    return of(pathSegments.toList(), pathSegments.isDir());
  }

  public boolean isEmpty() {
    return "/".equals(encoded);
  }

  public List<String> segments() {
    return segments;
  }

  public boolean isDirectory() {
    return isDir;
  }

  public String percentEncoded() {
    return encoded;
  }

  @Nonnull public final String filename() {
    return isDirectory() ? "" : segments().get(segments().size() - 1);
  }

  @Override public String toString() {
    return segments().toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Path) {
      Path that = (Path) o;
      return this.percentEncoded().equals(that.percentEncoded());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return percentEncoded().hashCode();
  }


  private static Path of(List<String> segments, boolean isDir) {
    String encoded;
    if (segments.isEmpty()) {
      encoded = "/";
    } else {
      String[] encodedSegments = new String[segments.size()];
      for (int i = 0; i < segments.size(); i++) {
        encodedSegments[i] = PercentEncoder.encodePathSegment(segments.get(i));
      }
      encoded = (isDir ? DIRECTORY_JOINER : FILE_JOINER).join(encodedSegments);
    }

    return new Path(
        segments,
        isDir,
        encoded);
  }

  private static class PathSegments {

    private final LinkedList<String> segments = new LinkedList<>();
    private boolean isDir = false;

    public void add(String segment) {
      if (segment.isEmpty()) {
        isDir = true;
      } else {
        if (SINGLE_DOT.matcher(segment).matches()) {
          isDir = true;
        } else if (DOUBLE_DOT.matcher(segment).matches()) {
          if (!segments.isEmpty()) {
            segments.remove();
          }
          isDir = true;
        } else {
          segments.add(segment);
          isDir = false;
        }
      }
    }

    public List<String> toList() {
      return Collections.unmodifiableList(segments);
    }

    public boolean isDir() {
      return isDir;
    }
  }
}
