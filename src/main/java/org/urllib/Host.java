package org.urllib;

public interface Host {

  /**
   * Returns the Url's host, encoded so that it can be passed to methods such as
   * {@link java.net.InetAddress#getByName(String)}.
   *
   * <table>
   * <tr>
   * <th>Type</td>
   * <th>URL</td>
   * <th>Host.toString()</td>
   * <th>Host.name()</td>
   * </tr>
   * <tr>
   * <td>ASCII DNS</td>
   * <td>http://duckduckgo.com/</td>
   * <td>duckduckgo.com</td>
   * <td>duckduckgo.com</td>
   * </tr>
   * <tr>
   * <td>International</td>
   * <td>http://кот.ru/</td>
   * <td>кот.ru</td>
   * <td>xn--j1aim.ru</td>
   * </tr>
   * <tr>
   * <td>IPv4</td>
   * <td>http://10.20.30.40/</td>
   * <td>10.20.30.40</td>
   * <td>10.20.30.40</td>
   * </tr>
   * <tr>
   * <td>IPv6</td>
   * <td>http://[2001:db8::1:0:0:1]/</td>
   * <td>2001:db8::1:0:0:1</td>
   * <td>2001:db8::1:0:0:1</td>
   * </tr>
   * </table>
   *
   * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2.2">RFC 3986#3.2.2</a>
   */
  String name();

}
