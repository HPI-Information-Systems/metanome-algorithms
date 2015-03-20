package de.metanome.algorithms.order.types;

import java.util.Collection;

import com.google.common.primitives.UnsignedBytes;

/**
 *
 * Utility class for column permutations that are represented as byte arrays.
 *
 * @author Philipp Langer
 *
 */
public class ByteArrayPermutations {

  /**
   * Takes two column permutations p1 and p2, each represented as an array of bytes of size l, with
   * the same prefix of size l-1 (i.e., the first l-1 elements of p1 and p2 are the same) and
   * returns a new column permutation of size l+1.
   *
   * The new column permutation contains as the first l column indices all column indices of p1, and
   * as the last column index, the last column index of p2.
   * <p>
   * Example: <br>
   * p1: [00000000, 00000001, 00000010] <br>
   * p2: [00000000, 00000010, 00000011] <br>
   * {@link ByteArrayPermutations#join}(p1, p2) => [00000000, 00000001, 00000010, 00000011]
   *
   * @param first column permutation of size l
   * @param second column permutation of size l
   * @return column permutation of size l+1
   */
  public static byte[] join(final byte[] p1, final byte[] p2) {
    final byte[] newPermutation = new byte[p1.length + 1];
    System.arraycopy(p1, 0, newPermutation, 0, p1.length);
    newPermutation[p1.length] = p2[p2.length - 1];
    return newPermutation;
  }

  /**
   * Takes two column permutations, represented as byte arrays, and checks whether they are
   * disjoint, i.e., if they have any columns in common.
   * <p>
   * Example:<br>
   * p1: [00000000, 00000001] <br>
   * p2: [00000010, 00000000] <br>
   * {@link ByteArrayPermutations#disjoint}(p1, p2) => false <br>
   * {@link ByteArrayPermutations#disjoint}(p1[0], p2[0]) => true
   *
   * @param first any column permutation
   * @param second any column permutation
   * @return boolean, indicating whether first and second are disjoint
   */
  public static boolean disjoint(final byte[] first, final byte[] second) {
    for (final byte c1 : first) {
      for (final byte c2 : second) {
        if (c1 == c2) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Returns the prefix of a permutation.
   *
   * Returns a byte[] of length 0, if provided permutation is of length 1.
   *
   * @param permutation
   * @return
   */
  public static byte[] prefix(final byte[] permutation) {
    if (permutation.length == 1) {
      return new byte[0];
    }
    final byte[] prefix = new byte[permutation.length - 1];
    for (int i = 0; i < permutation.length - 1; i++) {
      prefix[i] = permutation[i];
    }
    return prefix;
  }

  public static String permutationToBinaryString(final byte[] permutation) {
    if (permutation.length < 1) {
      return "[]";
    }
    final StringBuffer s = new StringBuffer(permutation.length);
    s.append("[");
    s.append(String.format("%8s", Integer.toBinaryString(permutation[0] & 0xFF)).replace(' ', '0'));
    for (int i = 1; i < permutation.length; i++) {
      s.append(", ");
      s.append(String.format("%8s", Integer.toBinaryString(permutation[i] & 0xFF))
          .replace(' ', '0'));
    }
    s.append("]");
    return s.toString();
  }

  /**
   *
   * Returns a string representation for a byte array permutation as integers.
   * <p>
   *
   * @param permutation
   * @return string representation of permutation
   */
  public static String permutationToIntegerString(final byte[] permutation) {
    return "[" + UnsignedBytes.join(", ", permutation) + "]";
  }

  public static String permutationListToIntegerString(final Collection<byte[]> list) {
    if (list.isEmpty()) {
      return "[]";
    }
    final StringBuffer sb = new StringBuffer();
    for (final byte[] b : list) {
      sb.append(ByteArrayPermutations.permutationToIntegerString(b)).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  public static byte[][] prefixes(final byte[] permutation) {
    final byte[][] prefixes = new byte[permutation.length - 1][];
    for (int i = 0; i < permutation.length - 1; i++) {
      final int prefixLength = permutation.length - (1 + i);
      prefixes[i] = new byte[prefixLength];
      for (int j = 0; j < prefixLength; j++) {
        prefixes[i][j] = permutation[j];
      }
    }

    return prefixes;
  }

  public static byte[][] subPermutations(final byte[] permutation) {
    final byte[][] subPermutations = new byte[permutation.length][permutation.length - 1];
    for (int j = 0; j < permutation.length - 1; j++) {
      final int limit = permutation.length - 1 - j;
      for (int i = 0; i < limit; i++) {
        subPermutations[i][j] = permutation[j];
      }
      for (int k = limit; k < permutation.length; k++) {
        subPermutations[k][j] = permutation[j + 1];
      }
    }
    return subPermutations;
  }

  public static byte[] suffix(final byte[] permutation) {
    return new byte[] {permutation[permutation.length - 1]};
  }

  /**
   *
   * Returns an index after @param fromIndex into @param lhs which is the next index in lhs after
   * lhs contains @param validRhs, starting from @param fromIndex
   *
   *
   * For example: lhs := 1837492 validRhs := 374 findOccurrenceOf(validRhs, lhs, 0) = 5
   *
   * Returns -1 if no such index exists.
   *
   * @param validRhs
   * @param lhs
   * @return
   */
  public static int findOccurrenceOf(final byte[] validRhs, final byte[] lhs, final int fromIndex) {

    if ((validRhs.length + fromIndex) > lhs.length) {
      return -1;
    }

    // find first occurrence of validRhs in lhs
    int index = -1;
    for (int i = fromIndex; i < lhs.length; i++) {
      if (lhs[i] == validRhs[0]) {
        index = i;
      }
    }

    if (index == -1 || (index + (validRhs.length - 1)) > lhs.length - 1) {
      // validRhs[0] not found in lhs or
      // remaining lhs not large enough for validRhs to be fully matched
      return -1;
    }

    for (int i = 0; i < validRhs.length; i++) {
      if (validRhs[i] == lhs[index]) {
        index++;
      } else {
        return -1;
      }
    }

    return index;
  }

}
