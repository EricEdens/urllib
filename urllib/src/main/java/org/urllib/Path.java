package org.urllib;

import java.util.List;
import javax.annotation.Nonnull;

/**
 * A hierarchical URL component that typically represents a location on a file system.
 *
 * <p>The following constraints exist:
 *
 * <ul>
 * <li>Paths are absolute</li>
 * <li>Segments are separated by the forward slash (byte 0x2F). Backslashes are converted to forward
 * slashes while parsing.</li>
 * <li>Empty path components are not allowed.</li>
 * </ul>
 *
 * <p>As a result of these rules, all of the following yield the same path of {@code /a/b/}:
 *
 * <pre>{@code
 *   Path.of("a/b/");
 *   Path.of("/a/b/");
 *   Path.of("a", "b/");
 *   Path.of("a", "b", "");
 *   Path.of("\a\b\");
 *   Path.of("/a/", "////b/");
 * }</pre>
 */
public interface Path {

  /**
   * Returns {@code true} if the path is the root path.
   * <pre>{@code
   *   assertTrue(Path.of("").isEmpty());
   *   assertTrue(Path.of("/").isEmpty());
   *   assertFalse(Path.of("/a").isEmpty());
   * }</pre>
   *
   * <p>The first and second example are true, and both equal to each other,
   * since this class enforces that all paths are absolute.
   */
  boolean isEmpty();

  /**
   * Returns all of the path segments, including the filename (if present). The segments
   * will not be percent encoded.
   *
   * <pre>{@code
   *   assertEquals(Arrays.asList("a"), Path.of("/a").segments());
   *   assertEquals(Arrays.asList("a"), Path.of("/a/").segments());
   *   assertEquals(Arrays.asList("a", "b"), Path.of("/a/b").segments());
   *   assertEquals(Arrays.asList("a", "b"), Path.of("/a/b/").segments());
   *   assertEquals(Arrays.asList("a", "b", "c"), Path.of("/a/b/c").segments());
   * }</pre>
   */
  @Nonnull List<String> segments();

  /**
   * Returns {@code true} if the path terminates in a forward slash.
   * <pre>{@code
   *   assertTrue(Path.of("/a/").isDirectory());
   *   assertFalse(Path.of("/a").isDirectory());
   * }</pre>
   */
  boolean isDirectory();

  /**
   * Returns the path's filename if present, otherwise the empty string. The result will not
   * be percent encoded.
   */
  @Nonnull String filename();

  @Nonnull String encoded();

  @Nonnull Path resolve(String ref);
}
