package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.Locale;
import javax.annotation.Nullable;

@AutoValue
public abstract class SplitUrl {

  private static final CodepointMatcher slash = CodepointMatcher.or('/', '\\');

  @Nullable public abstract String scheme();
  @Nullable public abstract String authority();
  @Nullable public abstract String path();
  @Nullable public abstract String query();
  @Nullable public abstract String fragment();
  @Nullable public abstract Type urlType();

  public static SplitUrl split(String url) {

    String trim = CodepointMatcher.WHITESPACE.trim(url);

    if (trim.isEmpty()) {
      return SplitUrl.builder()
          .path("")
          .urlType(Type.PATH_RELATIVE)
          .build();
    }

    Builder builder;
    if (CodepointMatcher.ALPHA.matches(trim.charAt(0))) {
      builder = fullUrlOrRelativePath(trim);
    } else if (slash.matches(trim.charAt(0))) {
      if (trim.length() > 1 && slash.matches(trim.charAt(1))) {
        builder = authority(trim, 0).urlType(Type.PROTOCOL_RELATIVE);
      } else {
        builder = path(trim, 0).urlType(Type.PATH_ABSOLUTE);
      }
    } else if (trim.charAt(0) == '#') {
      builder = fragment(trim, 0).urlType(Type.FRAGMENT);
    } else {
      builder = path(trim, 0).urlType(Type.PATH_RELATIVE);
    }
    return builder.build();
  }

  private static Builder fullUrlOrRelativePath(String url) {
    for (int i = 1; i < url.length(); i++) {
      char c = url.charAt(i);
      if (CodepointMatcher.ALPHANUMERIC.matches(c) || c == '+' || c == '-' || c == '.') {
        continue;
      } else if (c == ':') {
        return authority(url, i + 1)
            .scheme(url.substring(0, i).toLowerCase(Locale.US))
            .urlType(Type.FULL);
      } else {
        break;
      }
    }

    return path(url, 0)
        .urlType(Type.PATH_RELATIVE);
  }

  private static Builder authority(String url, int start) {

    while (start < url.length() && slash.matches(url.charAt(start))) {
      start++;
    }

    if (start >= url.length()) {
      throw new IllegalArgumentException("URL missing authority: " + url);
    }

    Builder builder = null;
    int i;
    for (i = start; i < url.length(); i++) {
      char c = url.charAt(i);
      if (c == '/' || c == '\\') {
        builder = path(url, i);
        break;
      } else if (c == '?') {
        builder = query(url, i);
        break;
      } else if (c == '#') {
        builder = fragment(url, i);
        break;
      }
    }

    if (builder == null) {
      builder = builder();
    }

    return builder.authority(url.substring(start, i));
  }

  private static Builder path(String url, int start) {
    Builder builder = null;
    int i;
    loop:
    for (i = start; i < url.length(); i++) {
      char c = url.charAt(i);
      if (c == '?') {
        builder = query(url, i);
        break;
      } else if (c == '#') {
        builder = fragment(url, i);
        break;
      }
    }

    if (builder == null) {
      builder = builder();
    }

    if (i > start) {
      builder.path(url.substring(start, i));
    }

    return builder;
  }

  private static Builder query(String url, int start) {
    Builder builder = null;
    int i;
    start++;
    for (i = start; i < url.length(); i++) {
      if (url.charAt(i) == '#') {
        builder = fragment(url, i);
        break;
      }
    }

    if (builder == null) {
      builder = builder();
    }

    return builder.query(url.substring(start, i));
  }

  private static Builder fragment(String url, int start) {
    return builder().fragment(url.substring(start + 1));
  }

  static Builder builder() {
    return new AutoValue_SplitUrl.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    public abstract Builder scheme(String scheme);
    public abstract Builder authority(String authority);
    public abstract Builder path(String path);
    public abstract Builder query(String query);
    public abstract Builder fragment(String fragment);
    public abstract Builder urlType(Type type);
    public abstract SplitUrl build();
  }
}
