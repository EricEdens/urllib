package org.urllib;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.urllib.internal.Authority;
import org.urllib.internal.Port;

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
 * <p>Java's {@link URI} is tough to navigate. If you need a {@link URI}, Urllib has you covered:
 * <pre>{@code
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

  private final Scheme scheme;
  private final int port;
  private final Authority authority;
  private final Path path;
  private final Query query;
  private final Fragment fragment;
  private final String encoded;

  private Url(Builder builder) {
    this.scheme = builder.scheme;
    this.authority = builder.authority;
    this.port = builder.port;
    this.path = builder.path;
    this.query = builder.query;
    this.fragment = builder.fragment;
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
   * Returns the Url's scheme.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.1">RFC 3986#3.1</a>
   */
  @Nonnull public Scheme scheme() {
    return scheme;
  }

  /**
   * Returns the Url's host.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2.2">RFC 3986#3.2.2</a>
   */
  @Nonnull public Host host() {
    return authority.host();
  }

  /**
   * Returns the Url's port.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2.3">RFC 3986#3.2.3</a>
   */
  @Nonnegative public int port() {
    return port;
  }

  /**
   * Returns the Url's query.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986#3.3</a>
   */
  @Nonnull public Path path() {
    return path;
  }

  /**
   * Returns the Url's query.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.4">RFC 3986#3.4</a>
   */
  @Nonnull public Query query() {
    return query;
  }

  /**
   * Returns the Url's fragment, not encoded.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.5">RFC 3986#3.5</a>
   */
  @Nonnull public Fragment fragment() {
    return fragment;
  }

  /**
   * Re-assemble the Url with an encoding that is compliant with
   * <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>.
   */
  public String percentEncoded() {
    return encoded;
  }

  public static final class Builder {

    private Scheme scheme;
    private int port;
    private Authority authority;
    private Path path = Path.empty();
    private Query query = Query.empty();
    private Fragment fragment = Fragment.empty();

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
      this.path = Path.of(splittableSegments);
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
      this.fragment = Fragment.of(fragment);
      return this;
    }

    public Url create() {
      return new Url(this);
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

      sb.append(path.percentEncoded());

      if (!query.isEmpty()) {
        sb.append('?').append(query.percentEncoded());
      }

      if (!fragment.isEmpty()) {
        sb.append('#').append(fragment.percentEncoded());
      }

      return sb.toString();
    }
  }
}
