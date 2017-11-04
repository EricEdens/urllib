package org.urllib;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.urllib.internal.CodepointMatcher;

/**
 * A hierarchical URL component that typically represents a location on a file system.
 *
 * <p>The following constraints exist:
 *
 * <ul>
 * <li>Paths are absolute</li>
 * <li>Segments are separated by the forward slash (byte 0x2F). Backslashes are converted to forward
 * slashes while parsing.</li>
 * <li>Empty path components are not allowed.</li>
 * </ul>
 *
 * <p>As a result of these rules, all of the following yield the same path of {@code /a/b/}:
 *
 * <pre>{@code
 *   Path.of("a/b/");
 *   Path.of("/a/b/");
 *   Path.of("a", "b/");
 *   Path.of("a", "b", "");
 *   Path.of("\a\b\");
 *   Path.of("/a/", "////b/");
 * }</pre>
 */
public final class Path {

  private static final Path EMPTY = new Path(new PathBuilder());

  @Nonnull private final List<String> segments;
  private final boolean isDir;

  /**
   * Creates a {@link Path} by joining {@code segments}.
   *
   * <ul>
   * <li>Backslashes (\) are converted to forward slashes (/)</li>
   * <li>Segments are then split at the slash character.</li>
   * <li>Empty path components are removed.</li>
   * </ul>
   *
   * <p>To create a path with a trailing slash, either terminate the last segment with a slash,
   * or include empty segment as the last segment:
   *
   * <pre>{@code
   *   // Both generate `/path/`:
   *   Path.of("path/");
   *   Path.of("path", "");
   * }</pre>
   *
   * <p>The result will always be an absolute path, regardless of whether the first character
   * is a slash or not.
   *
   * <pre>{@code
   *   // All generate `/path`:
   *   Path.of("path");
   *   Path.of("/path");
   *   Path.of("./path");
   *   Path.of("../path");
   * }</pre>
   */
  static Path of(String... segments) {
    if (segments.length == 0) {
      return EMPTY;
    }

    PathBuilder pathBuilder = new PathBuilder();
    for (String segment : segments) {
      pathBuilder.splitAndAdd(segment);
    }

    return new Path(pathBuilder);
  }

  static Path empty() {
    return EMPTY;
  }

  private Path(PathBuilder pathBuilder) {
    this.segments = pathBuilder.segments;
    this.isDir = pathBuilder.isDir;
  }

  /**
   * Returns {@code true} if the path is the root path.
   * <pre>{@code
   *   assertTrue(Path.of("").isEmpty());
   *   assertTrue(Path.of("/").isEmpty());
   *   assertFalse(Path.of("/a").isEmpty());
   * }</pre>
   *
   * <p>The first and second example are true, and both equal to each other,
   * since this class enforces that all paths are absolute.
   */
  public boolean isEmpty() {
    return segments.isEmpty();
  }

  /**
   * Returns all of the path segments, including the filename (if present). The segments
   * will not be percent encoded.
   *
   * <pre>{@code
   *   assertEquals(Arrays.asList("a"), Path.of("/a").segments());
   *   assertEquals(Arrays.asList("a"), Path.of("/a/").segments());
   *   assertEquals(Arrays.asList("a", "b"), Path.of("/a/b").segments());
   *   assertEquals(Arrays.asList("a", "b"), Path.of("/a/b/").segments());
   *   assertEquals(Arrays.asList("a", "b", "c"), Path.of("/a/b/c").segments());
   * }</pre>
   */
  @Nonnull public List<String> segments() {
    return segments;
  }

  /**
   * Returns {@code true} if the path terminates in a forward slash.
   * <pre>{@code
   *   assertTrue(Path.of("/a/").isDirectory());
   *   assertFalse(Path.of("/a").isDirectory());
   * }</pre>
   */
  public boolean isDirectory() {
    return isDir;
  }

  /**
   * Returns the path's filename if present, otherwise the empty string. The result will not
   * be percent encoded.
   */
  @Nonnull public final String filename() {
    return isDirectory() ? "" : segments().get(segments().size() - 1);
  }

  @Nonnull @Override
  public String toString() {
    return segments.toString();
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Path) {
      Path that = (Path) o;
      return toString().equals(that.toString());
    }
    return false;
  }

  @Override public int hashCode() {
    return toString().hashCode();
  }

  private static class PathBuilder {

    private static final CodepointMatcher SLASH_MATCHER = CodepointMatcher.anyOf("/\\");
    private static final Pattern SINGLE_DOT = Pattern.compile("^\\.|%2[eE]$");
    private static final Pattern DOUBLE_DOT = Pattern.compile("^(\\.|%2[eE]){2}$");

    private final LinkedList<String> segments = new LinkedList<>();

    private boolean isDir = false;

    public void splitAndAdd(String segment) {
      if (SLASH_MATCHER.matchesAnyOf(segment)) {
        int i = 0;
        int j = 0;
        for (; j < segment.length(); j++) {
          if (SLASH_MATCHER.matches(segment.charAt(j))) {
            add(segment.substring(i, j));
            i = j + 1;
          }
        }
        add(segment.substring(i, j));
      } else {
        add(segment);
      }
    }

    private void add(String segment) {
      if (segment.isEmpty()) {
        isDir = true;
      } else if (SINGLE_DOT.matcher(segment).matches()) {
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
}
