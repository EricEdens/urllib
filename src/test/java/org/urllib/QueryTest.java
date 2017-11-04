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


}