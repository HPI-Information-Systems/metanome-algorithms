package de.metanome.algorithms.order.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Permutation {
  private int[] permutation;
  private Permutation prefix;
  private Permutation suffix;
  private final int totalNumColumns;

  public Permutation(final int[] columnIndices, final int numColumns) {
    this.permutation = columnIndices;
    this.prefix = null;
    this.totalNumColumns = numColumns;
  }

  public Permutation(final int size, final int numColumns) {
    this.permutation = new int[size];
    this.prefix = null;
    this.totalNumColumns = numColumns;
  }

  public Permutation subPermutationWithout(final int index) {
    if (index > this.totalNumColumns - 1) {
      throw new IllegalArgumentException("Index " + index + " is greater than number of columns: "
          + this.totalNumColumns + ".");
    }
    if (this.permutation.length <= 1) {
      throw new IllegalArgumentException(
          "Permutations of length <= 1 do not have any subpermutations.");
    }
    final int[] newPermutation = new int[this.size() - 1];
    int j = 0;
    for (int i = 0; i < this.permutation.length; i++) {
      if (this.permutation[i] == index) {
        continue;
      }
      newPermutation[j] = this.permutation[i];
      j++;
    }
    return new Permutation(newPermutation, this.totalNumColumns);
  }

  public List<Permutation> subPermutations() {
    final List<Permutation> subPermutations = new ArrayList<Permutation>();
    for (final int index : this.permutation) {
      subPermutations.add(this.subPermutationWithout(index));
    }
    return subPermutations;
  }

  public Permutation prefix() {
    if (this.prefix == null) {
      this.prefix =
          new Permutation(Arrays.copyOfRange(this.permutation, 0, this.permutation.length - 1),
              this.totalNumColumns);
    }
    return this.prefix;
  }

  public Permutation suffix() {
    if (this.suffix == null) {
      final int[] suffixPermutation = {this.permutation[this.size() - 1]};
      this.suffix = new Permutation(suffixPermutation, this.totalNumColumns);
    }
    return this.suffix;
  }

  @Override
  public String toString() {
    return "[" + Arrays.toString(this.permutation) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(this.permutation);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final Permutation other = (Permutation) obj;
    if (!Arrays.equals(this.permutation, other.permutation)) {
      return false;
    }
    return true;
  }

  public int[] getPermutation() {
    return this.permutation;
  }

  public void setPermutation(final int[] permutation) {
    this.permutation = permutation;
  }

  public int size() {
    return this.permutation.length;
  }

  public boolean disjoint(final Permutation other) {
    for (final int i : this.permutation) {
      for (final int j : other.permutation) {
        if (i == j) {
          return false;
        }
      }
    }
    return true;
  }

}
