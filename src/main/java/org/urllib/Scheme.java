package org.urllib;

import javax.annotation.Nonnull;

public final class Scheme {

  @Nonnull private final String name;
  private final int port;

  public static final Scheme HTTP = new Scheme("http", 80);
  public static final Scheme HTTPS = new Scheme("https", 443);

  public Scheme(@Nonnull String name, int port) {
    this.name = name;
    this.port = port;
  }

  @Nonnull public String name() {
    return name;
  }

  public int defaultPort() {
    return port;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Scheme scheme = (Scheme) o;

    if (port != scheme.port) return false;
    return name.equals(scheme.name);
  }

  @Override public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + port;
    return result;
  }

  @Override public String toString() {
    return "Scheme{" +
        "name='" + name + '\'' +
        ", port=" + port +
        '}';
  }
}
