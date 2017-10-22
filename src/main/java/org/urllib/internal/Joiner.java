package org.urllib.internal;

import com.google.auto.value.AutoValue;
import java.util.Arrays;
import java.util.Iterator;

@AutoValue
public abstract class Joiner {

  abstract String seperator();
  abstract String prefix();
  abstract String suffix();

  public static Joiner on(char seperator) {
    return on(String.valueOf(seperator));
  }

  public static Joiner on(String seperator) {
    return on(seperator, "", "");
  }

  public static Joiner on(String seperator, String prefix, String suffix) {
    return new AutoValue_Joiner(seperator, prefix, suffix);
  }

  public String join(String... segments) {
    return join(Arrays.asList(segments));
  }

  public String join(Iterable<String> segments) {
    StringBuilder sb = new StringBuilder(prefix());
    for (Iterator<String> iterator = segments.iterator(); iterator.hasNext(); ) {
      String segment = iterator.next();
      sb.append(segment);
      if (iterator.hasNext()) {
        sb.append(seperator());
      }
    }
    sb.append(suffix());
    return sb.toString();
  }
}
