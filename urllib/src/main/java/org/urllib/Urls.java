package org.urllib;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.annotation.Nonnull;
import org.urllib.internal.authority.Authority;
import org.urllib.internal.PercentDecoder;
import org.urllib.internal.PercentEncoder;
import org.urllib.internal.Scheme;
import org.urllib.internal.SplitUrl;
import org.urllib.internal.Strings;
import org.urllib.internal.Type;

/**
 * This class consists of {@code static} utility methods for operating
 * on URL-related objects.
 */
public final class Urls {

  /**
   * Given a valid {@code http} or {@code https} URL, performs the minimal amount
   * of escaping to return a {@link java.net.URI}.
   *
   * <p>The scheme, host, and port are checked for correctness.
   * The input must be {@code http} or {@code https}, have a valid port (if present),
   * and the domain must be a valid ASCII DNS, IDN, IPv4, or IPv6 host name. If these conditions
   * are not met, then an {@link IllegalArgumentException} is thrown. The following are
   * examples of inputs that would cause an {@link IllegalArgumentException}.
   *
   * <table>
   *   <tr>
   *     <th>Invalid input</td>
   *     <th>Reason</th>
   *   </tr>
   *   <tr>
   *     <td>ldap://host.com</td>
   *     <td>Scheme must be http or https</td>
   *   </tr>
   *   <tr>
   *     <td>http://host.com:-1</td>
   *     <td>Invalid port</td>
   *   </tr>
   *   <tr>
   *     <td>http://host..com</td>
   *     <td>Invalid ASCII DNS name</td>
   *   </tr>
   *   <tr>
   *     <td>http://256.256.256.256</td>
   *     <td>Invalid IPv4</td>
   *   </tr>
   *   <tr>
   *     <td>http://[zz::99]</td>
   *     <td>Invalid IPv6</td>
   *   </tr>
   * </table>
   *
   * <p>If the scheme, host, and port are valid, then a minimal amount of sanitizing
   * and escaping is performed to create a {@link URI}:
   *
   * <ul>
   * <li>Leading and trailing whitespace is removed: <code>[\t\n\f\r ]*</code></li>
   * <li>Internal linebreaks are removed: <code>[\n\r][\t\n\f\r ]*</code></li>
   * <li>Backslashes are corrected before the authority and within the path.</li>
   * <li>Characters not allowed by RFC 3986 are escaped.</li>
   * <li>Userinfo is removed.</li>
   * </ul>
   *
   * The following are examples of input and output:
   *
   * <table>
   *   <tr>
   *     <th>Input</td>
   *     <th>Output</th>
   *   </tr>
   *   <tr>
   *     <td>http://❤</td>
   *     <td>http://xn--qei</td>
   *   </tr>
   *   <tr>
   *     <td>http:\\host\path\?q=\#\</td>
   *     <td>http://host/path/?q=%5C#%5C</td>
   *   </tr>
   *   <tr>
   *     <td>http://test.org/res?signature=a+b=&init=a a</td>
   *     <td>http://test.org/res?signature=a+b=&init=a%20a</td>
   *   </tr>
   *   <tr>
   *     <td>http://host/path;/?q=;|</td>
   *     <td>http://host/path;/?q=;%7C</td>
   *   </tr>
   *   <tr>
   *     <td>https://en.wikipedia.org/wiki/A*</td>
   *     <td>https://en.wikipedia.org/wiki/A*</td>
   *   </tr>
   *   <tr>
   *     <td>https://en.wikipedia.org/wiki/C++</td>
   *     <td>https://en.wikipedia.org/wiki/C++</td>
   *   </tr>
   *   <tr>
   *     <td>https://en.wikipedia.org/wiki/❄</td>
   *     <td>https://en.wikipedia.org/wiki/%E2%9D%84</td>
   *   </tr>
   *   <tr>
   *     <td>http://host/%2e</td>
   *     <td>http://host/%2e</td>
   *   </tr>
   *   <tr>
   *     <td>http://host/%zz</td>
   *     <td>http://host/%25zz</td>
   *   </tr>
   *   <tr>
   *     <td>http://FA::0:dd</td>
   *     <td>http://[fa::dd]</td>
   *   </tr>
   *   <tr>
   *     <td>http://user:pass@host.com:90</td>
   *     <td>http://host.com:90</td>
   *   </tr>
   * </table>
   *
   * @throws IllegalArgumentException if the input is not an http or https URL; the domain is
   * not valid ASCII DNS, IDN, IPv4, or IPv6; or the port is invalid.
   */
  @Nonnull public static URI createURI(@Nonnull String fullUrl) {
    String escaped = escape(fullUrl);

    try {
      return new URI(escaped);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  @Nonnull public static String escape(@Nonnull String url) {
    String trim = Strings.sanitizeWhitespace(url);
    SplitUrl split = SplitUrl.split(trim);

    if (split.urlType() != Type.FULL) {
      throw new IllegalArgumentException(
          "Not a full URL: " + url);
    }

    StringBuilder sb = new StringBuilder();

    if (split.scheme() != null) {
      String scheme = split.scheme().toLowerCase(Locale.US);
      if (scheme.equals("http") || scheme.equals("https")) {
        sb.append(scheme).append(':');
      } else {
        throw new IllegalArgumentException(
            "Only http and https schemes are supported. Input: " + url);
      }
    }

    if (split.authority() != null) {
      sb.append("//").append(Authority.split(split.authority()));
    }

    if (split.path() != null) {
      sb.append(PercentEncoder.reEncodePath(split.path()).replace('\\', '/'));
    }

    if (split.query() != null) {
      sb.append('?').append(PercentEncoder.reEncodeQuery(split.query()));
    }

    if (split.fragment() != null) {
      sb.append('#').append(PercentEncoder.reEncodeFragment(split.fragment()));
    }

    return sb.toString();
  }

  public static UrlBuilder http(String host) {
    return new UrlBuilder(Scheme.HTTP, host);
  }

  public static UrlBuilder https(String host) {
    return new UrlBuilder(Scheme.HTTPS, host);
  }

  @Nonnull public static Url parse(String url) {
    SplitUrl split = SplitUrl.split(Strings.sanitizeWhitespace(url));
    if (split.urlType() != Type.FULL) {
      throw new IllegalArgumentException("URL must have a scheme and host. Eg: http://host.com/");
    }

    UrlBuilder builder = new UrlBuilder(Scheme.valueOf(split.scheme()), split.authority());

    if (!Strings.isNullOrEmpty(split.path())) {
      builder.path(Path.parse(split.path()));
    }

    if (!Strings.isNullOrEmpty(split.query())) {
      builder.query(Query.parse(split.query()));
    }

    if (!Strings.isNullOrEmpty(split.fragment())) {
      builder.fragment(PercentDecoder.decodeAll(split.fragment()));
    }

    return builder.create();
  }

  private Urls() {}
}
