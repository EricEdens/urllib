package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@AutoValue
public abstract class Path {

  private static final CodepointMatcher SLASH_MATCHER = CodepointMatcher.anyOf("/\\");
  private static final Pattern SINGLE_DOT = Pattern.compile("^\\.|%2[eE]$");
  private static final Pattern DOUBLE_DOT = Pattern.compile("^(\\.|%2[eE]){2}$");

  private static final Path EMPTY = of(Collections.<String>emptyList(), true);

  public abstract List<String> segments();
  public abstract boolean isDir();
  public abstract String encoded();

  public static Path empty() {
    return EMPTY;
  }

  private static Path of(List<String> segments, boolean isDir) {
    return new AutoValue_Path(
        segments,
        isDir,
        encode(segments, isDir));
  }

  public static Path split(String... segments) {

    if (segments.length == 0) {
      return EMPTY;
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

  private static String encode(List<String> segments, boolean isDir) {
    String display;
    if (segments.isEmpty()) {
      display = "/";
    } else {
      Joiner joiner = Joiner.on("/", "/", isDir ? "/" : "");
      String[] encodedSegments = new String[segments.size()];
      for (int i = 0; i < segments.size(); i++) {
        encodedSegments[i] = PercentEncoder.encodePathSegment(segments.get(i));
      }
      display = joiner.join(encodedSegments);
    }
    return display;
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
      return segments;
    }

    public boolean isDir() {
      return isDir;
    }
  }
}
