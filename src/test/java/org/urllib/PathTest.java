package org.urllib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PathTest {

  @Test
  public void empty() {
    Path expected = Path.empty();

    assertEquals("/", expected.percentEncoded());

    assertEquals(expected, Path.of());
    assertEquals(expected, Path.of(""));
    assertEquals(expected, Path.of("/"));
    assertEquals(expected, Path.of("\\"));
    assertEquals(expected, Path.of("", ""));
    assertEquals(expected, Path.of("", "", ""));
  }

  @Test
  public void isDir() {
    assertTrue(Path.of("/a", "").isDirectory());
    assertTrue(Path.of("/a/").isDirectory());

    assertFalse(Path.of("/a").isDirectory());
    assertFalse(Path.of("a").isDirectory());
  }

  @Test
  public void encodeQueryAndFragment() {
    assertEquals("/%3F/%23", Path.of("?", "#").percentEncoded());
  }

  @Test
  public void cleanIncorrectSlashes() {
    assertEquals("/", Path.of("\\").percentEncoded());
    assertEquals("/path", Path.of("\\path").percentEncoded());
  }

  @Test
  public void removeDotSegments() {
    assertEquals("/", Path.of(".").percentEncoded());
    assertEquals("/", Path.of("..").percentEncoded());
    assertEquals("/", Path.of("/parent/..").percentEncoded());
    assertEquals("/parent/", Path.of("/parent/.").percentEncoded());
    assertEquals("/parent/", Path.of("/parent/%2e").percentEncoded());
    assertEquals("/parent/", Path.of("/parent/%2e/").percentEncoded());
    assertEquals("/parent/dir", Path.of("/parent/%2e/dir").percentEncoded());
    assertEquals("/dir", Path.of("/parent/%2e%2E/dir").percentEncoded());
  }

}