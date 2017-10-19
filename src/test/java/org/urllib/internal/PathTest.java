package org.urllib.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PathTest {

  @Test
  public void empty() {
    Path expected = Path.empty();

    assertEquals("/", expected.encoded());

    assertEquals(expected, Path.split());
    assertEquals(expected, Path.split(""));
    assertEquals(expected, Path.split("/"));
    assertEquals(expected, Path.split("\\"));
    assertEquals(expected, Path.split("", ""));
    assertEquals(expected, Path.split("", "", ""));
  }

  @Test
  public void isDir() {
    assertTrue(Path.split("/a", "").isDir());
    assertTrue(Path.split("/a/").isDir());

    assertFalse(Path.split("/a").isDir());
    assertFalse(Path.split("a").isDir());
  }

  @Test
  public void encodeQueryAndFragment() {
    assertEquals("/%3F/%23", Path.split("?", "#").encoded());
  }

  @Test
  public void cleanIncorrectSlashes() {
    assertEquals("/", Path.split("\\").encoded());
    assertEquals("/path", Path.split("\\path").encoded());
  }

  @Test
  public void removeDotSegments() {
    assertEquals("/", Path.split(".").encoded());
    assertEquals("/", Path.split("..").encoded());
    assertEquals("/", Path.split("/parent/..").encoded());
    assertEquals("/parent/", Path.split("/parent/.").encoded());
    assertEquals("/parent/", Path.split("/parent/%2e").encoded());
    assertEquals("/parent/", Path.split("/parent/%2e/").encoded());
    assertEquals("/parent/dir", Path.split("/parent/%2e/dir").encoded());
    assertEquals("/dir", Path.split("/parent/%2e%2E/dir").encoded());
  }

}