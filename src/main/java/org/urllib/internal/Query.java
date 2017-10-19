package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.annotation.Nullable;

public class Query {

  private final List<KeyValue> params;

  private Query() {
    this.params = new LinkedList<>();
  }

  public String encoded() {
    StringJoiner joiner = new StringJoiner("&");
    for (KeyValue param : params) {
      joiner.add(param.encoded());
    }
    return joiner.toString();
  }

  public static Query create(Map<String, String> paramMap) {
    Query query = new Query();
    paramMap.forEach((key, val) -> query.params.add(KeyValue.create(key, val)));
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

      if (value() == null || value().isEmpty()) {
        return PercentEncoder.encodeQueryComponentNoPlusForSpace(key());
      } else {
        return PercentEncoder.encodeQueryComponentNoPlusForSpace(key())
            + '=' + PercentEncoder.encodeQueryComponentNoPlusForSpace(value());
      }
    }
  }
}
