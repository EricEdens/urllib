package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.urllib.Query;
import org.urllib.Query.KeyValue;

public class Queries {

  private static final Query empty = of(Collections.<KeyValue>emptyList());

  public static Query create(Map<String, String> paramMap) {
    List<KeyValue> params = new ArrayList<>(paramMap.size());
    for (Entry<String, String> param : paramMap.entrySet()) {
      params.add(create(param.getKey(), param.getValue()));
    }
    return of(params);
  }

  public static Query empty() {
    return empty;
  }

  public static Query of(List<KeyValue> params) {
    return ImmutableQuery.create(params);
  }

  public static Query parse(String query) {
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
          params.add(create(key, ""));
        } else {
          String key = (p == equal)
              ? ""
              : PercentDecoder.decodeAll(query.substring(p, equal));
          String value = (i == equal + 1)
              ? ""
              : PercentDecoder.decodeAll(query.substring(equal + 1, i));
          params.add(create(key, value));
        }
        equal = -1;
        p = i + 1;
      } else if (query.charAt(i) == '=' && equal == -1) {
        equal = i;
      }
    }
    return of(params);
  }

  public static KeyValue create(String key, String value) {
    return new AutoValue_Queries_ImmutableKeyValue(key, Strings.nullToEmpty(value));
  }

  @AutoValue
  abstract static class ImmutableKeyValue implements KeyValue {
  }

  @AutoValue
  abstract static class ImmutableQuery implements Query {

    static Query create(List<KeyValue> params) {
      return new AutoValue_Queries_ImmutableQuery(
          Collections.unmodifiableList(params), toMap(params), encode(params));
    }

    private static Map<String, String> toMap(List<KeyValue> params) {
      Map<String, String> map = new HashMap<>();
      for (KeyValue keyValue : params) {
        if (!map.containsKey(keyValue.key())) {
          map.put(keyValue.key(), keyValue.value());
        }
      }
      return Collections.unmodifiableMap(map);
    }

    private static String encode(List<KeyValue> params) {
      StringBuilder sb = new StringBuilder();
      for (Iterator<KeyValue> iterator = params.iterator(); iterator.hasNext(); ) {
        KeyValue param = iterator.next();
        sb.append(PercentEncoder.encodeQueryComponentNoPlusForSpace(param.key()));
        if (param.value() != null && !param.value().isEmpty()) {
          sb.append('=')
              .append(PercentEncoder.encodeQueryComponentNoPlusForSpace(param.value()));
        }
        if (iterator.hasNext()) {
          sb.append('&');
        }
      }
      return sb.toString();
    }

    @Override public boolean isEmpty() {
      return params().isEmpty();
    }
  }
}
