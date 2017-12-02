package org.urllib;

import java.net.URI;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A <a href="https://en.wikipedia.org/wiki/URL">uniform resource locator (Url)</a> that is
 * immutable, compliant with <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>, and
 * interops with Java's {@link URI}.
 *
 * <h3>The Builder</h3>
 *
 * <p>Use the builder to create a Url from scratch.  For example, this code creates a search
 * for Wolfram Alpha using fancy unicode characters:<pre>{@code
 *
 *   Url url = Url.https("www.wolframalpha.com")
 *                .path("input/")
 *                .query("i", "π²")
 *                .create();
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
 *                    .create()
 *                    .uri();
 * }</pre>
 *
 * <p>The current implementation supports HTTP and HTTPS URLs, but future implementations may
 * support additional schemes.
 *
 * @since 1.0
 */
public interface Url {

  /**
   * Returns the Url's scheme in lowercase.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.1">RFC 3986#3.1</a>
   */
  @Nonnull String scheme();

  /**
   * Returns the Url's host.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2.2">RFC 3986#3.2.2</a>
   */
  @Nonnull Host host();

  /**
   * Returns the Url's port.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2.3">RFC 3986#3.2.3</a>
   */
  @Nonnegative int port();

  /**
   * Returns the Url's path.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986#3.3</a>
   */
  @Nonnull Path path();

  /**
   * Returns the Url's query.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.4">RFC 3986#3.4</a>
   */
  @Nonnull Query query();

  /**
   * Returns the Url's fragment, not escaped.
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.5">RFC 3986#3.5</a>
   */
  @Nonnull String fragment();

  /**
   * Returns this URL as a {@link java.net.URI}.
   */
  @Nonnull URI uri();
}
