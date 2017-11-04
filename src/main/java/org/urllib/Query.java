package org.urllib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Query {

  private static final Query empty = of(Collections.<KeyValue>emptyList());

  @Nonnull private final List<KeyValue> params;

  private Query(@Nonnull List<KeyValue> params) {
    this.params = params;
  }

  static Query create(Map<String, String> paramMap) {
    List<KeyValue> params = new ArrayList<>(paramMap.size());
    for (Entry<String, String> param : paramMap.entrySet()) {
      params.add(KeyValue.create(param.getKey(), param.getValue()));
    }
    return of(params);
  }

  static Query empty() {
    return empty;
  }

  private static Query of(List<KeyValue> params) {
    return new Query(Collections.unmodifiableList(params));
  }

  public List<KeyValue> params() {
    return params;
  }

  public Map<String, String> asMap() {
    Map<String, String> map = new HashMap<>();
    for (KeyValue keyValue : params()) {
      if (!map.containsKey(keyValue.key())) {
        map.put(keyValue.key(), keyValue.value());
      }
    }
    return Collections.unmodifiableMap(map);
  }

  @Override public String toString() {
    return params.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Query) {
      Query that = (Query) o;
      return this.params.equals(that.params);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return params.hashCode();
  }

  public boolean isEmpty() {
    return params.isEmpty();
  }

  public static final class KeyValue {

    @Nonnull private final String key;
    @Nullable private final String value;

    public KeyValue(@Nonnull String key, @Nullable String value) {
      this.key = key;
      this.value = value;
    }

    public static KeyValue create(String key, String value) {
      return new KeyValue(key, value);
    }

    @Nonnull public String key() {
      return key;
    }

    @Nullable public String value() {
      return value;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      KeyValue keyValue = (KeyValue) o;

      if (!key.equals(keyValue.key)) return false;
      return value != null ? value.equals(keyValue.value) : keyValue.value == null;
    }

    @Override public int hashCode() {
      int result = key.hashCode();
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
    }

    @Override public String toString() {
      return "KeyValue{" +
          "key='" + key + '\'' +
          ", value='" + value + '\'' +
          '}';
    }
  }
}
