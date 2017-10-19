package org.urllib;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import org.urllib.internal.Authority;
import org.urllib.internal.Path;
import org.urllib.internal.PercentEncoder;
import org.urllib.internal.Port;
import org.urllib.internal.Query;
import org.urllib.internal.Scheme;

/**
 * A <a href="https://en.wikipedia.org/wiki/URL">uniform resource locator (Url)</a> that is
 * immutable, compliant with <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>, and
 * interops with Java's {@link URI} and {@link URL}.
 *
 * <h3>The Builder</h3>
 *
 * <p>Use the builder to create a Url from scratch.  For example, this code creates a search
 * for Wolfram Alpha using fancy unicode characters:<pre>{@code
 *
 *   Url url = Url.https("www.wolframalpha.com")
 *                .path("input/")
 *                .query("i", "π²")
 *                .build();
 *
 *   System.out.println(url);
 * }</pre>
 *
 * which prints: <a href="https://www.wolframalpha.com/input/?i=%CF%80%C2%B2">
 * <code>https://www.wolframalpha.com/input/?i=%CF%80%C2%B2</code></a>
 *
 * <h3>java.net.URI</h3>
 *
 * <p>Java's {@link URI} is a minefield of hostile constructors, unclear encoding
 * rules, and surprise exceptions. If you need a {@link URI}, Urllib has you covered:<pre>{@code
 *
 *   URI moliere = Url.http("wikipedia.org")
 *                    .path("wiki", "Molière")
 *                    .toURI();
 * }</pre>
 *
 * <p>The current implementation supports HTTP and HTTPS URLs, but future implementations may
 * support additional schemes.
 *
 * @since 1.0
 */
public final class Url {

  private final String encoded;

  private Url(Builder builder) {
    this.encoded = builder.toString();
  }

  public static Builder http(String host) {
    return new Builder()
        .scheme(Scheme.HTTP)
        .port(Scheme.HTTP.defaultPort())
        .host(host);
  }

  public static Builder https(String host) {
    return new Builder()
        .scheme(Scheme.HTTPS)
        .port(Scheme.HTTPS.defaultPort())
        .host(host);
  }

  /**
   * Re-assemble the Url with an encoding that is compliant with
   * <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>.
   */
  @Override public String toString() {
    return encoded;
  }

  public static final class Builder {

    private Scheme scheme;
    private int port;
    private Authority authority;
    private Path path = Path.empty();
    private Query query;
    private String fragment;

    private Builder() {}

    private Builder scheme(Scheme scheme) {
      this.scheme = scheme;
      return this;
    }

    public Builder port(int port) {
      this.port = Port.validateOrThrow(port);
      return this;
    }

    private Builder host(String host) {
      this.authority = Authority.split(host);
      if (this.authority.port() != -1) {
        port(authority.port());
      }
      return this;
    }

    public Builder path(String... splittableSegments) {
      this.path = Path.split(splittableSegments);
      return this;
    }

    public Builder query(String key, String value) {
      this.query = Query.create(Collections.singletonMap(key, value));
      return this;
    }

    public Builder query(Map<String, String> query) {
      this.query = Query.create(query);
      return this;
    }

    public Builder fragment(String fragment) {
      this.fragment = fragment;
      return this;
    }

    public URI toURI() {
      try {
        return new URI(toString());
      } catch (URISyntaxException e) {
        // Reaching this would be a bug in this library.
        throw new AssertionError(e);
      }
    }

    @Override public String toString() {
      StringBuilder sb =
          new StringBuilder()
              .append(scheme.name())
              .append("://")
              .append(authority.host());

      if (port != scheme.defaultPort()) {
        sb.append(':').append(port);
      }

      if (path != null) {
        sb.append(path.encoded());
      } else {
        sb.append('/');
      }

      if (query != null) {
        sb.append('?').append(query.encoded());
      }

      if (fragment != null) {
        sb.append('#').append(PercentEncoder.encodeFragment(fragment));
      }

      return sb.toString();
    }

  }
}
