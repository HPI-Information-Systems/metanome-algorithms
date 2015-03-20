package de.metanome.algorithms.order.check;

import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;

import org.apache.lucene.util.OpenBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithms.order.sorting.partitions.SortedPartition;

public class DependencyChecker {

  protected static Logger logger = LoggerFactory.getLogger(DependencyChecker.class);

  public static boolean checkOrderDependencyStrictlySmaller(final SortedPartition A,
      final SortedPartition B) {

    final long overlapping =
        OpenBitSet.andNotCount(A.getEquivalenceSetsBitRepresentation(),
            B.getEquivalenceSetsBitRepresentation());
    if (overlapping > 0) {
      logger.trace("Returned checkOrderDependency early (bitset optimization): {} ~/~> {}", A, B);
      return false;
    }

    long j = 0;
    for (long i = 0; i < A.size(); i++) {
      final LongOpenHashBigSet partitionA = A.get(i);

      // try to match the current partition of A
      // with partitions from B
      long matchingBs = 0;
      while (matchingBs < partitionA.size64()) {
        final LongOpenHashBigSet partitionB = B.get(j);

        // TODO: This should not be necessary anymore, because of bitset check above
        if (partitionB.size64() > (A.get(i).size64() - matchingBs)) {
          // the size of the current partition of B is greater than
          // the size of non-matched row indices of current partition of A
          return false;
        }

        for (final long rowIndexB : partitionB) {
          if (!partitionA.contains(rowIndexB)) {
            // row indices don't match: i-th smallest partition of A
            return false;
          }
          matchingBs++;
        }
        j++;
      }

    }
    return true;
  }

  /**
   *
   * Checks if ODC(A,B) holds and saves the result in position 0 of the returned array. If ODC(A,B)
   * is violated by a swap among A and B, position 1 of the returned array contains true, otherwise,
   * it contains false.
   *
   * This method is less efficient than
   * {@link DependencyChecker#checkOrderDependencyStrictlySmaller(SortedPartition, SortedPartition)}
   * .
   *
   * @return
   */
  public static boolean[] checkOrderDependencyForSwapStrictlySmaller(final SortedPartition A,
      final SortedPartition B) {

    final boolean[] result = new boolean[2];
    boolean merge = false;
    final long overlapping =
        OpenBitSet.andNotCount(A.getEquivalenceSetsBitRepresentation(),
            B.getEquivalenceSetsBitRepresentation());
    if (overlapping > 0) {
      logger.trace(
          "Returned checkOrderDependency early (bitset optimization detected a merge): {} ~/~> {}",
          A, B);
      merge = true;
    }

    final SortedPartition Acopy =
        SortedPartition
        .copyFromEquivalenceClasses(A.getNumRows(), A.getOrderedEquivalenceClasses());
    final SortedPartition Bcopy =
        SortedPartition
        .copyFromEquivalenceClasses(B.getNumRows(), B.getOrderedEquivalenceClasses());

    long visited = 0;

    long i = 0;
    long j = 0;

    LongOpenHashBigSet partitionA = Acopy.get(i);
    LongOpenHashBigSet partitionB = Bcopy.get(j);
    long nonMatchedA = partitionA.size64();
    long nonMatchedB = partitionB.size64();

    while (visited < A.getNumRows()) {
      if (nonMatchedB < nonMatchedA) {
        // map the row indices of the current equivalence class of B to those in the current
        // equivalence class of A
        for (final long rowIndexB : partitionB) {
          visited++;
          if (partitionA.contains(rowIndexB)) {
            partitionA.remove(rowIndexB);
            nonMatchedA--;
            nonMatchedB--;
          } else {
            // swap detected
            result[0] = false;
            result[1] = true;
            return result;
          }
        }
        if (j < Bcopy.size() - 1) {
          // get the next equivalence class, if it exists
          j++;
          partitionB = Bcopy.get(j);
          nonMatchedB += partitionB.size64();
        }
      } else if (nonMatchedA < nonMatchedB) {
        // map the row indices of the current equivalence class of A to those in the current
        // equivalence class of B
        for (final long rowIndexA : partitionA) {
          visited++;
          if (partitionB.contains(rowIndexA)) {
            partitionB.remove(rowIndexA);
            nonMatchedA--;
            nonMatchedB--;
          } else {
            // swap detected
            result[0] = false;
            result[1] = true;
            return result;
          }
        }
        if (i < Acopy.size() - 1) {
          i++;
          partitionA = Acopy.get(i);
          nonMatchedA += partitionA.size64();
        }
      } else { // nonMatchedA == nonMatchedB
        for (final long rowIndexA : partitionA) { // could iterate over partitionB as well
          visited++;
          if (partitionB.contains(rowIndexA)) {
            partitionB.remove(rowIndexA);
            nonMatchedA--;
            nonMatchedB--;
          } else {
            // swap detected
            result[0] = false;
            result[1] = true;
            return result;
          }
        }
        if (i < A.size() - 1 && j < B.size() - 1) {
          i++;
          j++;
          partitionA = Acopy.get(i);
          partitionB = Bcopy.get(j);
          nonMatchedA += partitionA.size64();
          nonMatchedB += partitionB.size64();
        }
      }
    }

    if (merge) {
      // there was a merge (OD invalid), no swap (otherwise, method would have returned early)
      result[0] = false;
      result[1] = false;
      return result;
    }

    // there was no merge (otherwise, method would have returned above), and no swap (otherwise,
    // method would have returned early)
    result[0] = true;
    result[1] = false;



    return result;
  }



  /**
   *
   * check if the order dependency A~>B is valid. Uses the logical equivalence <br>
   * A~><sub>&lt;</sub>B <=> B~>_<sub>&lt;=</sub>A
   *
   * @param A
   * @param B
   * @return
   */
  public static boolean checkOrderDependencySmallerEqual(final SortedPartition A,
      final SortedPartition B) {
    return checkOrderDependencyStrictlySmaller(B, A);
  }

  /**
   *
   * check if the order dependency A~>B is valid and, if not, is invalidated by a swap. Uses the
   * logical equivalence <br>
   * A~><sub>&lt;</sub>B <=> B~>_<sub>&lt;=</sub>A
   *
   * @param A
   * @param B
   * @return
   */
  public static boolean[] checkOrderDependencyForSwapSmallerEqual(final SortedPartition A,
      final SortedPartition B) {
    return checkOrderDependencyForSwapStrictlySmaller(B, A);
  }

}
