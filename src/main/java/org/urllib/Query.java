package org.urllib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.urllib.internal.PercentDecoder;
import org.urllib.internal.PercentEncoder;

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

  static Query parse(String query) {
    if (query.isEmpty()) return empty();
    query = query.replace('+', ' ');
    List<KeyValue> params = new LinkedList<>();
    int p = 0;
    int equal = -1;
    for (int i = 0; i <= query.length(); i++) {
      if (i == query.length() || query.charAt(i) == '&') {
        if (i == p) {
        } else if (equal == -1) {
          String key = PercentDecoder.decodeAll(query.substring(p, i));
          params.add(KeyValue.create(key, ""));
        } else {
          String key = (p == equal)
              ? ""
              : PercentDecoder.decodeAll(query.substring(p, equal));
          String value = (i == equal + 1)
              ? ""
              : PercentDecoder.decodeAll(query.substring(equal + 1, i));
          params.add(KeyValue.create(key, value));
        }
        equal = -1;
        p = i + 1;
      } else if (query.charAt(i) == '=' && equal == -1) {
        equal = i;
      }
    }
    return of(params);
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
    StringBuilder sb = new StringBuilder();
    for (Iterator<KeyValue> iterator = params.iterator(); iterator.hasNext(); ) {
      KeyValue param = iterator.next();
      sb.append(PercentEncoder.encodeQueryComponent(param.key()));
      if (param.value() != null && !param.value().isEmpty()) {
        sb.append('=')
            .append(PercentEncoder.encodeQueryComponent(param.value()));
      }
      if (iterator.hasNext()) {
        sb.append('&');
      }
    }
    return sb.toString();
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
