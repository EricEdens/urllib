package org.urllib;

import javax.annotation.Nonnull;
import org.urllib.internal.PercentEncoder;

public final class Fragment {

  private static final Fragment empty = new Fragment("", "");

  @Nonnull private final String original;
  @Nonnull private final String encoded;

  private Fragment(@Nonnull String original, @Nonnull String encoded) {
    this.original = original;
    this.encoded = encoded;
  }

  static Fragment empty() {
    return empty;
  }

  static Fragment of(@Nonnull String original) {
    return new Fragment(original, PercentEncoder.encodeFragment(original));
  }

  public boolean isEmpty() {
    return encoded.isEmpty();
  }

  @Override public String toString() {
    return original;
  }

  @Nonnull public String percentEncoded() {
    return encoded;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Fragment fragment = (Fragment) o;

    return encoded.equals(fragment.encoded);
  }

  @Override public int hashCode() {
    return encoded.hashCode();
  }
}
