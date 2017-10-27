package org.urllib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class PathTest {

  @Test
  public void isEmpty() {
    assertTrue(Path.empty().isEmpty());
    assertTrue(Path.of().isEmpty());
    assertTrue(Path.of("").isEmpty());
    assertTrue(Path.of("/").isEmpty());
    assertTrue(Path.of("", "").isEmpty());

    assertEquals(Path.empty(), Path.of());
    assertEquals(Path.empty(), Path.of(""));
    assertEquals(Path.empty(), Path.of("/"));
    assertEquals(Path.empty(), Path.of("", ""));
  }

  @Test
  public void pathIsAlwaysAbsolute() {
    assertEquals("/a", Path.of("/a").toString());
    assertEquals("/a", Path.of("a").toString());
    assertEquals("/a", Path.of("./a").toString());
    assertEquals("/a", Path.of("../a").toString());
  }

  @Test
  public void emptySegmentsAreRemoved() {
    assertEquals("/a/b/c/", Path.of("a/", "/b", "//c//").toString());
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
    assertEquals("/%3F/%23", Path.of("?", "#").toString());
  }

  @Test
  public void cleanIncorrectSlashes() {
    assertEquals("/", Path.of("\\").toString());
    assertEquals("/path", Path.of("\\path").toString());
  }

  @Test
  public void removeDotSegments() {
    assertEquals("/", Path.of(".").toString());
    assertEquals("/", Path.of("..").toString());
    assertEquals("/", Path.of("/parent/..").toString());
    assertEquals("/parent/", Path.of("/parent/.").toString());
    assertEquals("/parent/", Path.of("/parent/%2e").toString());
    assertEquals("/parent/", Path.of("/parent/%2e/").toString());
    assertEquals("/parent/dir", Path.of("/parent/%2e/dir").toString());
    assertEquals("/dir", Path.of("/parent/%2e%2E/dir").toString());
  }

  @Test
  public void segmentsIncludeFilename() {
    assertEquals(Arrays.asList("a"), Path.of("/a").segments());
    assertEquals(Arrays.asList("a"), Path.of("/a/").segments());
    assertEquals(Arrays.asList("a", "b"), Path.of("/a/b").segments());
    assertEquals(Arrays.asList("a", "b"), Path.of("/a/b/").segments());
    assertEquals(Arrays.asList("a", "b", "c"), Path.of("/a/b/c").segments());
  }

  @Test
  public void filenameIsNotEncoded() {
    assertEquals("résumé.html", Path.of("/docs/résumé.html").filename());
  }

  @Test
  public void filenameDefaultsToEmptyString() {
    assertEquals("", Path.of("/lib/").filename());
  }

  @Test
  public void equalsAndHashcode() {
    Path a1 = Path.of("a", "b", "c/");
    Path a2 = Path.of("/a/", "/b/", "/c/");
    Path a3 = Path.of("/a", "b", "c", "");

    assertEquals(a1, a2);
    assertEquals(a2, a3);
    assertEquals(a1, a3);

    assertEquals(a1.hashCode(), a2.hashCode());
    assertEquals(a2.hashCode(), a3.hashCode());
    assertEquals(a1.hashCode(), a3.hashCode());

    Path b1 = Path.of("a");

    assertNotEquals(a1, b1);
    assertNotEquals(a2, b1);
    assertNotEquals(a3, b1);

    assertNotEquals(a1.hashCode(), b1.hashCode());
    assertNotEquals(a2.hashCode(), b1.hashCode());
    assertNotEquals(a3.hashCode(), b1.hashCode());
  }
}