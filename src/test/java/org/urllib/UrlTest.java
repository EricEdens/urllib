package org.urllib;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;

public class UrlTest {

  @Test
  public void builderFullUrl() {
    assertEquals("http://localhost:8080/files/pdf/report.pdf?user=8123#Reports",
        Url.http("localhost:8080")
            .path("/files/pdf/", "report.pdf")
            .query(Collections.singletonMap("user", "8123"))
            .fragment("Reports")
            .toString());

    assertEquals("https://www.google.co.jp/search?q=%E5%AD%90%E7%8C%AB",
        Url.https("www.google.co.jp")
            .path("/search")
            .query(Collections.singletonMap("q", "子猫"))
            .toString());

    assertEquals("http://wikipedia.org/wiki/Moli%C3%A8re",
        Url.http("wikipedia.org")
            .path("/wiki", "Molière")
            .toString());

    assertEquals("https://twitter.com/search?q=%F0%9F%A4%A8",
        Url.https("twitter.com")
            .path("/search")
            .query(Collections.singletonMap("q", "\uD83E\uDD28"))
            .toString());

    assertEquals("https://www.wolframalpha.com/input/?i=%CF%80%C2%B2",
        Url.https("www.wolframalpha.com")
            .path("input/")
            .query(Collections.singletonMap("i", "π²"))
            .toString());
  }

  @Test
  public void allowPortWithHost() {
    assertEquals("http://localhost:8080/", Url.http("localhost:8080").toString());
    assertEquals("http://localhost/", Url.http("localhost:80").toString());
  }

}