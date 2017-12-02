package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.urllib.Host;
import org.urllib.Path;
import org.urllib.Query;
import org.urllib.Url;

@AutoValue
public abstract class UrllibUrl implements Url {

  @Nonnull public abstract String scheme();
  @Nonnegative abstract int defaultPort();
  @Nonnegative public abstract int port();
  @Nonnull  public abstract Host host();
  @Nonnull public abstract Path path();
  @Nonnull public abstract Query query();
  @Nonnull public abstract String fragment();

  @Override @Nonnull public URI uri() {
    try {
      return new URI(toString());
    } catch (URISyntaxException e) {
      // Reaching this point would mean a bug in our url encoding.
      throw new AssertionError(
          "Please file a bug at https://github.com/EricEdens/urllib/issues");
    }
  }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder()
        .append(scheme())
        .append("://")
        .append(host().name());

    if (port() != defaultPort()) {
      sb.append(':').append(port());
    }

    sb.append(path());

    if (!query().isEmpty()) {
      sb.append('?').append(query());
    }

    if (!fragment().isEmpty()) {
      sb.append('#').append(PercentEncoder.encodeFragment(fragment()));
    }

    return sb.toString();
  }

  public static UrllibUrl create(String scheme, int defaultPort, int port, Host host, Path path,
      Query query, String fragment) {
    return new AutoValue_UrllibUrl(scheme, defaultPort, port, host, path, query, fragment);
  }

  UrllibUrl() {}

}
