package org.urllib.internal.authority;

class InvalidHostException extends IllegalArgumentException {

  InvalidHostException(String hostname, int i) {
    super(String.format("Invalid hostname: Illegal character at %d in %s.", i, hostname));
  }
}
