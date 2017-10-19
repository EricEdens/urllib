package org.urllib.internal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SplitUrlTest {

  @Test public void empty() {
    SplitUrl expected = SplitUrl.builder()
        .path("")
        .urlType(Type.PATH_RELATIVE)
        .build();
    assertEquals(expected, SplitUrl.split(""));
  }

  @Test public void length1() {
    assertEquals(onlyPath("/"), SplitUrl.split("/"));
    assertEquals(onlyPath("p"), SplitUrl.split("p"));
    assertEquals(onlyPath(":"), SplitUrl.split(":"));
    assertEquals(onlyQuery(""), SplitUrl.split("?"));
    assertEquals(onlyFragment(""), SplitUrl.split("#"));
  }


  @Test public void withScheme() {
    SplitUrl expected = SplitUrl.builder()
        .scheme("http")
        .authority("host")
        .urlType(Type.FULL)
        .build();
    assertEquals(expected, SplitUrl.split("http:host"));
    assertEquals(expected, SplitUrl.split("HTTP:host"));
    assertEquals(expected, SplitUrl.split("http:/host"));
    assertEquals(expected, SplitUrl.split("http://host"));
    assertEquals(expected, SplitUrl.split("http:\\host"));
    assertEquals(expected, SplitUrl.split("http:\\\\host"));
    assertEquals(expected, SplitUrl.split("http://\\host"));
  }

  @Test public void protocolRelative() {
    SplitUrl expected = SplitUrl.builder()
        .authority("host")
        .urlType(Type.PROTOCOL_RELATIVE)
        .build();
    assertEquals(expected, SplitUrl.split("//host"));
    assertEquals(expected, SplitUrl.split("///host"));
    assertEquals(expected, SplitUrl.split("\\\\host"));
  }

  @Test public void invalidOrMissingScheme() {
    assertEquals(onlyPath(":host"), SplitUrl.split(":host"));
    assertEquals(onlyPath("bad@scheme://host"), SplitUrl.split("bad@scheme://host"));
    assertEquals(onlyPath("://host"), SplitUrl.split("://host"));
  }

  @Test public void convertSchemeToLowerCase() {
    SplitUrl expected = SplitUrl.builder()
        .scheme("http")
        .authority("host")
        .urlType(Type.FULL)
        .build();
    assertEquals(expected, SplitUrl.split("http://host"));
    assertEquals(expected, SplitUrl.split("HttP://host"));
    assertEquals(expected, SplitUrl.split("HTTP://host"));
  }

  @Test public void httpRequestLine() {
    assertEquals(SplitUrl.builder()
        .path("/pages/index.html")
        .urlType(Type.PATH_ABSOLUTE)
        .build(), SplitUrl.split("/pages/index.html"));

    assertEquals(SplitUrl.builder()
        .path("/pages/index.html")
        .query("user=dan")
        .urlType(Type.PATH_ABSOLUTE)
        .build(), SplitUrl.split("/pages/index.html?user=dan"));
  }

  @Test public void authorityWithUserInfo() {
    assertEquals(SplitUrl.builder()
        .scheme("http")
        .authority("user:password@domain.com:90")
        .path("/path")
        .urlType(Type.FULL)
        .build(), SplitUrl.split("http://user:password@domain.com:90/path"));
  }

  @Test public void wrongDirectionSlashes() {
    assertEquals(SplitUrl.builder()
        .scheme("http")
        .authority("host")
        .path("\\path")
        .urlType(Type.FULL)
        .build(), SplitUrl.split("http:\\\\host\\path"));
  }

  @Test public void unicodeInUrl() {
    assertEquals(SplitUrl.builder()
        .scheme("http")
        .authority("猫.cn")
        .path("/餐饮")
        .query("q=美味的食物")
        .fragment("猫")
        .urlType(Type.FULL)
        .build(), SplitUrl.split("http://猫.cn/餐饮?q=美味的食物#猫"));
  }

  @Test public void onlyScheme() {
    try {
      SplitUrl.split("http:");
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("URL missing authority"));
    }
  }

  @Test public void onlyPath() {
    assertEquals(onlyPath(""), SplitUrl.split(""));
    assertEquals(onlyPath("/"), SplitUrl.split("/"));
    assertEquals(onlyPath("/"), SplitUrl.split("/"));
    assertEquals(onlyPath("a"), SplitUrl.split("a"));
    assertEquals(onlyPath("/a"), SplitUrl.split("/a"));
    assertEquals(onlyPath("/a/"), SplitUrl.split("/a/"));
  }

  @Test public void onlyQuery() {
    assertEquals(onlyQuery(""), SplitUrl.split("?"));
    assertEquals(onlyQuery("?"), SplitUrl.split("??"));
    assertEquals(onlyQuery("/path"), SplitUrl.split("?/path"));
    assertEquals(onlyQuery("http://url"), SplitUrl.split("?http://url"));
  }

  @Test public void onlyFragment() {
    assertEquals(onlyFragment(""), SplitUrl.split("#"));
    assertEquals(onlyFragment("/path"), SplitUrl.split("#/path"));
    assertEquals(onlyFragment("?query"), SplitUrl.split("#?query"));
  }

  private SplitUrl onlyFragment(String fragment) {
    return SplitUrl.builder()
        .urlType(Type.FRAGMENT)
        .fragment(fragment)
        .build();
  }

  private SplitUrl onlyQuery(String query) {
    return SplitUrl.builder()
        .urlType(Type.PATH_RELATIVE)
        .query(query)
        .build();
  }

  private SplitUrl onlyPath(String path) {
    boolean absolute = path.startsWith("/") || path.startsWith("\\");
    return SplitUrl.builder()
        .urlType(absolute ? Type.PATH_ABSOLUTE : Type.PATH_RELATIVE)
        .path(path)
        .build();
  }
}