package org.urllib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;
import org.urllib.internal.Paths;

public class PathTest {

  @Test public void isEmpty() {
    assertTrue(Paths.empty().isEmpty());
    assertTrue(Paths.of().isEmpty());
    assertTrue(Paths.of("").isEmpty());
    assertTrue(Paths.of("/").isEmpty());
    assertTrue(Paths.of("", "").isEmpty());

    assertEquals(Paths.empty(), Paths.of());
    assertEquals(Paths.empty(), Paths.of(""));
    assertEquals(Paths.empty(), Paths.of("/"));
    assertEquals(Paths.empty(), Paths.of("", ""));
  }

  @Test public void pathIsAlwaysAbsolute() {
    Path expected = Paths.of("/a");
    assertEquals(expected, Paths.of("a"));
    assertEquals(expected, Paths.of("./a"));
    assertEquals(expected, Paths.of("../a"));
  }

  @Test public void emptySegmentsAreRemoved() {
    assertEquals(Paths.of("/a/b/c/"), Paths.of("a/", "/b", "//c//"));
  }

  @Test public void isDir() {
    assertTrue(Paths.of("/a", "").isDirectory());
    assertTrue(Paths.of("/a/").isDirectory());

    assertFalse(Paths.of("/a").isDirectory());
    assertFalse(Paths.of("a").isDirectory());
  }

  @Test public void cleanIncorrectSlashes() {
    assertEquals(Paths.of("/"), Paths.of("\\"));
    assertEquals(Paths.of("/path"), Paths.of("\\path"));
    assertEquals(Paths.of("/path/"), Paths.of("\\path\\"));
  }

  @Test public void removeDotSegments() {
    assertEquals(Arrays.asList(), Paths.of(".").segments());
    assertEquals(Arrays.asList(), Paths.of("..").segments());
    assertEquals(Arrays.asList(), Paths.of("/parent/..").segments());
    assertEquals(Arrays.asList("parent"), Paths.of("/parent/.").segments());
    assertEquals(Arrays.asList("parent"), Paths.of("/parent/%2e").segments());
    assertEquals(Arrays.asList("parent"), Paths.of("/parent/%2e/").segments());
    assertEquals(Arrays.asList("parent", "dir"), Paths.of("/parent/%2e/dir").segments());
    assertEquals(Arrays.asList("dir"), Paths.of("/parent/%2e%2E/dir").segments());
  }

  @Test public void segmentsIncludeFilename() {
    assertEquals(Arrays.asList("a"), Paths.of("/a").segments());
    assertEquals(Arrays.asList("a"), Paths.of("/a/").segments());
    assertEquals(Arrays.asList("a", "b"), Paths.of("/a/b").segments());
    assertEquals(Arrays.asList("a", "b"), Paths.of("/a/b/").segments());
    assertEquals(Arrays.asList("a", "b", "c"), Paths.of("/a/b/c").segments());
  }

  @Test public void filenameIsNotEncoded() {
    assertEquals("résumé.html", Paths.of("/docs/résumé.html").filename());
  }

  @Test public void filenameDefaultsToEmptyString() {
    assertEquals("", Paths.of("/lib/").filename());
  }

  @Test public void equalsAndHashcode() {
    Path a1 = Paths.of("a", "b", "c/");
    Path a2 = Paths.of("/a/", "/b/", "/c/");
    Path a3 = Paths.of("/a", "b", "c", "");

    assertEquals(a1, a2);
    assertEquals(a2, a3);
    assertEquals(a1, a3);

    assertEquals(a1.hashCode(), a2.hashCode());
    assertEquals(a2.hashCode(), a3.hashCode());
    assertEquals(a1.hashCode(), a3.hashCode());

    Path b1 = Paths.of("a");

    assertNotEquals(a1, b1);
    assertNotEquals(a2, b1);
    assertNotEquals(a3, b1);

    assertNotEquals(a1.hashCode(), b1.hashCode());
    assertNotEquals(a2.hashCode(), b1.hashCode());
    assertNotEquals(a3.hashCode(), b1.hashCode());
  }
}