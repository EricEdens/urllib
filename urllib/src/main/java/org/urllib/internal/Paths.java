package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.urllib.Path;

public class Paths {

  /**
   * Creates a {@link org.urllib.Path} by joining {@code segments}.
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
  public static Path of(String... segments) {
    if (segments.length == 0) {
      return ImmutablePath.EMPTY;
    }

    PathBuilder pathBuilder = new PathBuilder();
    for (String segment : segments) {
      pathBuilder.splitAndAdd(segment, false);
    }

    return ImmutablePath.create(pathBuilder);
  }

  public static Path parse(String path) {
    return path.isEmpty()
        ? ImmutablePath.EMPTY
        : ImmutablePath.create(new PathBuilder().splitAndAdd(path, true));
  }

  public static Path empty() {
    return ImmutablePath.EMPTY;
  }

  static class PathBuilder {

    private static final CodepointMatcher SLASH_MATCHER = CodepointMatcher.anyOf("/\\");
    private static final Pattern SINGLE_DOT = Pattern.compile("^\\.|%2[eE]$");
    private static final Pattern DOUBLE_DOT = Pattern.compile("^(\\.|%2[eE]){2}$");

    private final LinkedList<String> segments;
    private boolean isDir;

    PathBuilder() {
      this(new LinkedList<String>(), false);
    }

    PathBuilder(LinkedList<String> segments, boolean isDir) {
      this.segments = segments;
      this.isDir = isDir;
    }

    PathBuilder splitAndAdd(String segment, boolean decode) {
      if (SLASH_MATCHER.matchesAnyOf(segment)) {
        int i = 0;
        int j = 0;
        for (; j < segment.length(); j++) {
          if (SLASH_MATCHER.matches(segment.charAt(j))) {
            add(segment.substring(i, j), decode);
            i = j + 1;
          }
        }
        return add(segment.substring(i, j), decode);
      } else {
        return add(segment, decode);
      }
    }

    private PathBuilder add(String segment, boolean decode) {
      if (segment.isEmpty()) {
        isDir = true;
      } else if (SINGLE_DOT.matcher(segment).matches()) {
        isDir = true;
      } else if (DOUBLE_DOT.matcher(segment).matches()) {
        if (!segments.isEmpty()) {
          segments.removeLast();
        }
        isDir = true;
      } else {
        segments.add(decode ? PercentDecoder.decodeAll(segment) : segment);
        isDir = false;
      }
      return this;
    }
  }

  @AutoValue
  abstract static class ImmutablePath implements Path {

    private static final Path EMPTY = create(Collections.<String>emptyList(), true);

    static Path create(PathBuilder builder) {
      return create(builder.segments, builder.isDir);
    }

    static Path create(List<String> segments, boolean isDir) {
      String filename = isDir ? "" : segments.get(segments.size() - 1);
      return new AutoValue_Paths_ImmutablePath(segments.isEmpty(), segments, isDir,
          filename, encode(isDir, segments));
    }

    private static String encode(boolean isDir, List<String> segments) {
      StringBuilder sb = new StringBuilder("/");
      for (Iterator<String> iterator = segments.iterator(); iterator.hasNext(); ) {
        String segment = iterator.next();
        sb.append(PercentEncoder.encodePathSegment(segment));
        if (iterator.hasNext() || isDir) {
          sb.append('/');
        }
      }
      return sb.toString();
    }

    @Nonnull @Override public Path resolve(String reference) {

      if (reference.isEmpty()) {
        return this;
      }

      loop:
      for (int i = 0; i < reference.length(); i++) {
        char c = reference.charAt(i);
        switch (c) {
          case ':':
            throw new IllegalArgumentException(
                "Paths.resolve can only be used with a relative or absolute path, not a full URL.");
          case '/':
          case '\\':
            if (i == 0) {
              return parse(reference);
            } else {
              break loop;
            }
        }
      }

      LinkedList<String> segments = new LinkedList<>(segments());
      if (!isDirectory()) {
        segments.removeLast();
      }
      return create(new PathBuilder(segments, true).splitAndAdd(reference, true));
    }
  }
}
