package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

public class Query {

  private final List<KeyValue> params;

  private Query() {
    this.params = new LinkedList<>();
  }

  public String encoded() {
    Joiner joiner = Joiner.on('&');
    String[] encodedPairs = new String[params.size()];
    for (int i = 0; i < params.size(); i++) {
      encodedPairs[i] = params.get(i).encoded();
    }
    return joiner.join(encodedPairs);
  }

  public static Query create(Map<String, String> paramMap) {
    Query query = new Query();
    for (Entry<String, String> param : paramMap.entrySet()) {
      query.params.add(KeyValue.create(param.getKey(), param.getValue()));
    }
    return query;
  }

  @AutoValue
  static abstract class KeyValue {

    abstract String key();
    @Nullable abstract String value();

    public static KeyValue create(String key, String value) {
      return new AutoValue_Query_KeyValue(key, value);
    }

    public String encoded() {

      if (Strings.isNullOrEmpty(value())) {
        return PercentEncoder.encodeQueryComponentNoPlusForSpace(key());
      } else {
        return PercentEncoder.encodeQueryComponentNoPlusForSpace(key())
            + '=' + PercentEncoder.encodeQueryComponentNoPlusForSpace(value());
      }
    }
  }
}
