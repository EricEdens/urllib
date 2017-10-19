package org.urllib;

public final class UrlException extends IllegalArgumentException {

  public UrlException() {
    super();
  }

  public UrlException(String s) {
    super(s);
  }

  public UrlException(String message, Throwable cause) {
    super(message, cause);
  }

  public UrlException(Throwable cause) {
    super(cause);
  }
}
