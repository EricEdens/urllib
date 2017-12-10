package org.urllib;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Url's query.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.4">RFC 3986#3.4</a>
 */
public interface Query {

  List<KeyValue> params();
  Map<String, String> asMap();
  boolean isEmpty();
  String encoded();

  interface KeyValue {
    @Nonnull String key();
    @Nullable String value();
  }
}
