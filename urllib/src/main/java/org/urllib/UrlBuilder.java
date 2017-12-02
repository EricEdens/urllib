package org.urllib;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.urllib.internal.Authority;
import org.urllib.internal.Port;
import org.urllib.internal.Scheme;
import org.urllib.internal.UrllibUrl;

public final class UrlBuilder {

  @Nonnull Scheme scheme;
  int port = -1;
  @Nonnull Authority authority;
  @Nonnull Path path = Path.empty();
  @Nonnull Query query = Query.empty();
  @Nonnull String fragment = "";

  UrlBuilder() {}

  UrlBuilder scheme(Scheme scheme) {
    this.scheme = scheme;
    return this;
  }

  public UrlBuilder port(int port) {
    this.port = Port.validateOrThrow(port);
    return this;
  }

  UrlBuilder host(String host) {
    this.authority = Authority.split(host);
    if (this.authority.port() != -1) {
      port(authority.port());
    }
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
