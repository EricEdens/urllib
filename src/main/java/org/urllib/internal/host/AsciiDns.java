package org.urllib.internal.host;

import com.google.auto.value.AutoValue;
import org.urllib.Host;

@AutoValue
public abstract class AsciiDns implements Host {

  public abstract String name();

  @Override public String toString() {
    return name();
  }

  public static AsciiDns create(String original) {return new AutoValue_AsciiDns(original);}
}
