package org.urllib;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;
import org.urllib.Query.KeyValue;

public class QueryTest {

  @Test public void retainOrderIfSupportedByMap() {
    Map<String, String> params = ImmutableMap.of(
        "a", "1",
        "b", "2"
    );
    Query query = Query.create(params);
    assertEquals(
        ImmutableList.of(KeyValue.create("a", "1"), KeyValue.create("b", "2")),
        query.params());
  }

  @Test public void parsePlusIsSpace() {
    Query expected = Query.create(ImmutableMap.of(
        "k1", " "
    ));
    assertEquals(expected, Query.parse("k1= "));
    assertEquals(expected, Query.parse("k1=+"));
    assertEquals(expected, Query.parse("k1=%20"));
  }

  @Test public void parseIncompleteKeyValuePairs() {
    Query expected = Query.create(ImmutableMap.of(
        "k1", "",
        "k2", ""
    ));
    assertEquals(expected, Query.parse("k1&k2"));
    assertEquals(expected, Query.parse("k1=&k2"));
    assertEquals(expected, Query.parse("k1&k2="));
    assertEquals(expected, Query.parse("k1&k2="));
    assertEquals(expected, Query.parse("k1=&k2="));
    assertEquals(expected, Query.parse("&k1=&k2="));
    assertEquals(expected, Query.parse("&k1&k2&"));
  }

  @Test public void parseRetainsOrderOfParams() {
    Query expected = Query.create(ImmutableMap.of(
        "k1", "a",
        "k2", "b"
    ));
    assertEquals(expected, Query.parse("k1=a&k2=b"));
  }

  @Test public void parseRemovesPercentEncodingAfterParsing() {
    Query expected = Query.create(ImmutableMap.of(
        "k1", "a",
        "k2", "b"
    ));
    assertEquals(expected, Query.parse("%6b1=a&%6b2=%62"));

    expected = Query.create(ImmutableMap.of(
        "k1", "="
    ));
    assertEquals(expected, Query.parse("k1=%3d"));

    expected = Query.create(ImmutableMap.of(
        "k1", "&"
    ));
    assertEquals(expected, Query.parse("k1=%26"));
  }

  @Test public void parseRetainsInvalidEscapes() {
    Query expected = Query.create(ImmutableMap.of(
        "k1", "%",
        "k2", "%z",
        "k3", "%zz"
    ));
    assertEquals(expected, Query.parse("k1=%&k2=%z&k3=%zz"));
  }
}