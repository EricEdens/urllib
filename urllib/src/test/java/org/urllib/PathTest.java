package org.urllib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class PathTest {

  @Test public void isEmpty() {
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

  @Test public void pathIsAlwaysAbsolute() {
    Path expected = Path.of("/a");
    assertEquals(expected, Path.of("a"));
    assertEquals(expected, Path.of("./a"));
    assertEquals(expected, Path.of("../a"));
  }

  @Test public void emptySegmentsAreRemoved() {
    assertEquals(Path.of("/a/b/c/"), Path.of("a/", "/b", "//c//"));
  }

  @Test public void isDir() {
    assertTrue(Path.of("/a", "").isDirectory());
    assertTrue(Path.of("/a/").isDirectory());

    assertFalse(Path.of("/a").isDirectory());
    assertFalse(Path.of("a").isDirectory());
  }

  @Test public void cleanIncorrectSlashes() {
    assertEquals(Path.of("/"), Path.of("\\"));
    assertEquals(Path.of("/path"), Path.of("\\path"));
    assertEquals(Path.of("/path/"), Path.of("\\path\\"));
  }

  @Test public void removeDotSegments() {
    assertEquals(Arrays.asList(), Path.of(".").segments());
    assertEquals(Arrays.asList(), Path.of("..").segments());
    assertEquals(Arrays.asList(), Path.of("/parent/..").segments());
    assertEquals(Arrays.asList("parent"), Path.of("/parent/.").segments());
    assertEquals(Arrays.asList("parent"), Path.of("/parent/%2e").segments());
    assertEquals(Arrays.asList("parent"), Path.of("/parent/%2e/").segments());
    assertEquals(Arrays.asList("parent", "dir"), Path.of("/parent/%2e/dir").segments());
    assertEquals(Arrays.asList("dir"), Path.of("/parent/%2e%2E/dir").segments());
  }

  @Test public void segmentsIncludeFilename() {
    assertEquals(Arrays.asList("a"), Path.of("/a").segments());
    assertEquals(Arrays.asList("a"), Path.of("/a/").segments());
    assertEquals(Arrays.asList("a", "b"), Path.of("/a/b").segments());
    assertEquals(Arrays.asList("a", "b"), Path.of("/a/b/").segments());
    assertEquals(Arrays.asList("a", "b", "c"), Path.of("/a/b/c").segments());
  }

  @Test public void filenameIsNotEncoded() {
    assertEquals("résumé.html", Path.of("/docs/résumé.html").filename());
  }

  @Test public void filenameDefaultsToEmptyString() {
    assertEquals("", Path.of("/lib/").filename());
  }

  @Test public void equalsAndHashcode() {
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