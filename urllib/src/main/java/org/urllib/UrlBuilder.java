package org.urllib;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.urllib.internal.Authority;
import org.urllib.internal.Port;
import org.urllib.internal.Scheme;
import org.urllib.internal.UrllibUrl;

/**
 * Use the builder to create a {@link Url} from scratch.  For example, this code creates a search
 * for Wolfram Alpha using fancy unicode characters:<pre>{@code
 *
 *   Url url = Urls.https("www.wolframalpha.com")
 *                 .path("input/")
 *                 .query("i", "π²")
 *                 .create();
 *
 *   System.out.println(url);
 * }</pre>
 *
 * which prints: <a href="https://www.wolframalpha.com/input/?i=%CF%80%C2%B2">
 * <code>https://www.wolframalpha.com/input/?i=%CF%80%C2%B2</code></a>
 *
 * @since 1.0
 */
public final class UrlBuilder {

  @Nonnull final Scheme scheme;
  int port = -1;
  @Nonnull final Authority authority;
  @Nonnull Path path = Path.empty();
  @Nonnull Query query = Query.empty();
  @Nonnull String fragment = "";

  UrlBuilder(@Nonnull Scheme scheme, @Nonnull String host) {
    this.scheme = scheme;
    this.authority = Authority.split(host);
    if (authority.port() != -1) {
      port(authority.port());
    }
  }

  public UrlBuilder port(int port) {
    this.port = Port.validateOrThrow(port);
    return this;
  }

  public UrlBuilder path(String... splittableSegments) {
    this.path = Path.of(splittableSegments);
    return this;
  }

  UrlBuilder path(Path path) {
    this.path = path;
    return this;
  }

  public UrlBuilder query(String key, String value) {
    this.query = Query.create(Collections.singletonMap(key, value));
    return this;
  }

  public UrlBuilder query(Map<String, String> query) {
    this.query = Query.create(query);
    return this;
  }

  UrlBuilder query(Query query) {
    this.query = query;
    return this;
  }

  public UrlBuilder fragment(String fragment) {
    this.fragment = fragment;
    return this;
  }

  public Url create() {
    if (this.port == -1) {
      this.port = scheme.defaultPort();
    }
    return UrllibUrl.create(scheme.name(), scheme.defaultPort(), port,
        authority.host(), path, query, fragment);
  }
}
