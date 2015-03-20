package de.metanome.algorithms.order.types;

import java.util.Arrays;

public class ByteArray {
  public final byte[] data;

  public ByteArray(final byte[] data) {
    if (data == null) {
      throw new NullPointerException();
    }
    this.data = data;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof ByteArray)) {
      return false;
    }
    return Arrays.equals(this.data, ((ByteArray) other).data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.data);
  }

  @Override
  public String toString() {
    return ByteArrayPermutations.permutationToIntegerString(this.data);
  }
}
