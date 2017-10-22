package org.urllib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.urllib.internal.Joiner;
import org.urllib.internal.PercentEncoder;
import org.urllib.internal.Strings;

public final class Query {

  private static final Query empty = of(Collections.<KeyValue>emptyList());

  @Nonnull private final List<KeyValue> params;
  @Nonnull private final String encoded;

  private Query(@Nonnull List<KeyValue> params, @Nonnull String encoded) {
    this.params = params;
    this.encoded = encoded;
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
    return new Query(Collections.unmodifiableList(params), encode(params));
  }

  private static String encode(List<KeyValue> params) {
    if (params.isEmpty()) return "";
    int length = params.size();
    String[] encodedPairs = new String[length];
    for (int i = 0; i < length; i++) {
      encodedPairs[i] = params.get(i).encoded();
    }
    return Joiner.on('&').join(encodedPairs);
  }

  public List<KeyValue> params() {
    return params;
  }

  public String percentEncoded() {
    return encoded;
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
      return this.percentEncoded().equals(that.percentEncoded());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return percentEncoded().hashCode();
  }

  public boolean isEmpty() {
    return percentEncoded().isEmpty();
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

    public String encoded() {
      if (Strings.isNullOrEmpty(value())) {
        return PercentEncoder.encodeQueryComponentNoPlusForSpace(key());
      } else {
        return PercentEncoder.encodeQueryComponentNoPlusForSpace(key())
            + '=' + PercentEncoder.encodeQueryComponentNoPlusForSpace(value());
      }
    }

    @Override public String toString() {
      return "KeyValue{" +
          "key='" + key + '\'' +
          ", value='" + value + '\'' +
          '}';
    }
  }
}
