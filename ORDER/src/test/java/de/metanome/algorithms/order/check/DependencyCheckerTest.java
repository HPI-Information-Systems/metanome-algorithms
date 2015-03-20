package de.metanome.algorithms.order.check;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import de.metanome.algorithms.order.sorting.partitions.SortedPartition;

public class DependencyCheckerTest {

  /**
   * no swap, violated by merge
   */
  @Test
  public void testCheckOrderDependencyForSwapInvalidMerge() {
    // Setup
    final int numRows = 6;
    final SortedPartition A = new SortedPartition(numRows);
    final SortedPartition B = new SortedPartition(numRows);

    A.addEquivalenceClassByValues(0, 1, 2);
    A.addEquivalenceClassByValues(3);
    A.addEquivalenceClassByValues(4, 5);

    B.addEquivalenceClassByValues(0, 1);
    B.addEquivalenceClassByValues(2, 3);
    B.addEquivalenceClassByValues(4, 5);

    // Expected values

    // ODC(A,B) is violated by a merge among A and B
    final boolean[] expectedResult = {false, false};

    // Execute functionality
    final boolean[] actualResult =
        DependencyChecker.checkOrderDependencyForSwapStrictlySmaller(A, B);
    // Check result
    assertTrue(Arrays.equals(actualResult, expectedResult));
    assertFalse(DependencyChecker.checkOrderDependencyStrictlySmaller(A, B));
  }

  /**
   * violated by swap
   */
  @Test
  public void testCheckOrderDependencyForSwapInvalidSwap() {
    // Setup
    final int numRows = 5;
    final SortedPartition A = new SortedPartition(numRows);
    final SortedPartition B = new SortedPartition(numRows);

    A.addEquivalenceClassByValues(0, 1);
    A.addEquivalenceClassByValues(2);
    A.addEquivalenceClassByValues(3);
    A.addEquivalenceClassByValues(4);

    B.addEquivalenceClassByValues(0, 1);
    B.addEquivalenceClassByValues(2, 4);
    B.addEquivalenceClassByValues(3);

    // Expected values

    // ODC(A,B) is violated by a swap among A and B
    final boolean[] expectedResult = {false, true};

    // Execute functionality
    final boolean[] actualResult =
        DependencyChecker.checkOrderDependencyForSwapStrictlySmaller(A, B);
    // Check result
    assertTrue(Arrays.equals(actualResult, expectedResult));
    assertFalse(DependencyChecker.checkOrderDependencyStrictlySmaller(A, B));
  }

  /**
   * violated by merge
   */
  @Test
  public void testCheckOrderDependencyForSwapInvalidMergeLastEqClass() {
    // Setup
    final int numRows = 6;
    final SortedPartition A = new SortedPartition(numRows);
    final SortedPartition B = new SortedPartition(numRows);

    A.addEquivalenceClassByValues(0, 1, 2);
    A.addEquivalenceClassByValues(3);
    A.addEquivalenceClassByValues(5);
    A.addEquivalenceClassByValues(4);

    B.addEquivalenceClassByValues(0, 1, 2, 3);
    B.addEquivalenceClassByValues(4, 5);

    // Expected values

    // ODC(A,B) is violated by a merge among A and B
    final boolean[] expectedResult = {false, false};

    // Execute functionality
    final boolean[] actualResult =
        DependencyChecker.checkOrderDependencyForSwapStrictlySmaller(A, B);
    // Check result
    assertTrue(Arrays.equals(actualResult, expectedResult));
    assertFalse(DependencyChecker.checkOrderDependencyStrictlySmaller(A, B));
  }

  /**
   * violated by swap
   */
  @Test
  public void testCheckOrderDependencyForSwapInvalidSwapSimple() {
    // Setup
    final int numRows = 2;
    final SortedPartition A = new SortedPartition(numRows);
    final SortedPartition B = new SortedPartition(numRows);

    A.addEquivalenceClassByValues(0);
    A.addEquivalenceClassByValues(1);

    B.addEquivalenceClassByValues(1);
    B.addEquivalenceClassByValues(0);

    // Expected values

    // ODC(A,B) is violated by a swap among A and B
    final boolean[] expectedResult = {false, true};

    // Execute functionality
    final boolean[] actualResult =
        DependencyChecker.checkOrderDependencyForSwapStrictlySmaller(A, B);
    // Check result
    assertTrue(Arrays.equals(actualResult, expectedResult));
    assertFalse(DependencyChecker.checkOrderDependencyStrictlySmaller(A, B));
  }

  /**
   * valid dependency
   */
  @Test
  public void testCheckOrderDependencyForSwapValid() {
    // Setup
    final int numRows = 8;
    final SortedPartition A = new SortedPartition(numRows);
    final SortedPartition B = new SortedPartition(numRows);

    A.addEquivalenceClassByValues(0, 1);
    A.addEquivalenceClassByValues(2, 3, 4);
    A.addEquivalenceClassByValues(5, 6, 7);

    B.addEquivalenceClassByValues(0, 1);
    B.addEquivalenceClassByValues(2, 3);
    B.addEquivalenceClassByValues(4);
    B.addEquivalenceClassByValues(5, 6);
    B.addEquivalenceClassByValues(7);

    // Expected values

    // ODC(A,B) is violated by a swap among A and B
    final boolean[] expectedResult = {true, false};

    // Execute functionality
    final boolean[] actualResult =
        DependencyChecker.checkOrderDependencyForSwapStrictlySmaller(A, B);
    // Check result
    assertTrue(Arrays.equals(actualResult, expectedResult));
    assertTrue(DependencyChecker.checkOrderDependencyStrictlySmaller(A, B));
    assertTrue(DependencyChecker.checkOrderDependencySmallerEqual(B, A));
  }


}
