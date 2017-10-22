package org.urllib;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;

public class QueryTest {

  @Test public void encodePlus() {
    assertEquals("%2B=%2B", encoded("+", "+"));
    assertEquals("%2B", encoded("+", null));
  }

  @Test public void encodeSemicolon() {
    assertEquals("%3B=%3B", encoded(";", ";"));
    assertEquals("%3B", encoded(";", null));
  }

  @Test public void encodeAmpersand() {
    assertEquals("%26=%26", encoded("&", "&"));
    assertEquals("%26", encoded("&", null));
  }

  @Test public void encodeEquals() {
    assertEquals("%3D=%3D", encoded("=", "="));
    assertEquals("%3D", encoded("=", null));
  }

  @Test public void encodeHash() {
    assertEquals("%23=%23", encoded("#", "#"));
    assertEquals("%23", encoded("#", null));
  }

  @Test public void encodeSpaceWithPercent() {
    assertEquals("%20=%20", encoded(" ", " "));
    assertEquals("%20", encoded(" ", null));
  }

  private String encoded(String key, String val) {
    return Query.create(Collections.singletonMap(key, val)).percentEncoded();
  }

}