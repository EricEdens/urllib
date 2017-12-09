package org.urllib.internal.authority;

public class Port {

  private static final int MIN_PORT = 1;
  private static final int MAX_PORT = (2 << 15) - 1;

  public static int validateOrThrow(String portString) {
    int port;
    try {
      port = Integer.parseInt(portString);
    } catch (NumberFormatException e) {
      throw portException(portString);
    }

    return validateOrThrow(port);
  }

  public static int validateOrThrow(int port) {
    if (port >= Port.MIN_PORT && port <= Port.MAX_PORT) {
      return port;
    }

    throw portException(String.valueOf(port));
  }

  private static IllegalArgumentException portException(String portString) {
    return new IllegalArgumentException(
        String.format("Invalid port in authority. Valid values are [%d-%d] inclusive. Found: %s",
            MIN_PORT, MAX_PORT, portString));
  }
}
