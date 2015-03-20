package de.metanome.algorithms.order.sorting.partitions;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;

import org.junit.Before;
import org.junit.Test;

public class SortedPartitionTest {

  int numRows;

  @Before
  public void setUp() {
    this.numRows = 8;
  }

  @Test
  public void testMultiply1() {
    // Setup
    final SortedPartition p1 = new SortedPartition(this.numRows);
    final long[] p1EqC1 = {0};
    final long[] p1EqC2 = {1};
    final long[] p1EqC3 = {2, 3, 4};
    final long[] p1EqC4 = {5, 6};
    final long[] p1EqC5 = {7};
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC1));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC2));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC3));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC4));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC5));

    final SortedPartition p2 = new SortedPartition(this.numRows);
    final long[] p2EqC1 = {4, 2, 7};
    final long[] p2EqC2 = {0, 5};
    final long[] p2EqC3 = {3};
    final long[] p2EqC4 = {6};
    final long[] p2EqC5 = {1};
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC1));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC2));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC3));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC4));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC5));

    // Expected Values
    final SortedPartition expectedP12 = new SortedPartition(this.numRows);
    final long[] p12EqC1 = {0};
    final long[] p12EqC2 = {1};
    final long[] p12EqC3 = {2, 4};
    final long[] p12EqC4 = {3};
    final long[] p12EqC5 = {5};
    final long[] p12EqC6 = {6};
    final long[] p12EqC7 = {7};
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC1));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC2));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC3));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC4));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC5));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC6));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC7));

    // Execute functionality
    final SortedPartition p12 = p1.multiply(p2);
    // Check result
    assertEquals(p12, expectedP12);
  }

  @Test
  public void testMultiply2() {
    // Setup
    final SortedPartition p1 = new SortedPartition(this.numRows);
    final long[] p1EqC1 = {0};
    final long[] p1EqC2 = {1};
    final long[] p1EqC3 = {2, 3, 4};
    final long[] p1EqC4 = {5, 6};
    final long[] p1EqC5 = {7};
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC1));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC2));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC3));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC4));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC5));

    final SortedPartition p2 = new SortedPartition(this.numRows);
    final long[] p2EqC1 = {4, 2, 5};
    final long[] p2EqC2 = {0, 7};
    final long[] p2EqC3 = {3};
    final long[] p2EqC4 = {6};
    final long[] p2EqC5 = {1};
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC1));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC2));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC3));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC4));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC5));

    // Expected Values
    final SortedPartition expectedP12 = new SortedPartition(this.numRows);
    final long[] p12EqC1 = {0};
    final long[] p12EqC2 = {1};
    final long[] p12EqC3 = {2, 4};
    final long[] p12EqC4 = {3};
    final long[] p12EqC5 = {5};
    final long[] p12EqC6 = {6};
    final long[] p12EqC7 = {7};
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC1));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC2));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC3));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC4));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC5));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC6));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC7));

    // Execute functionality
    final SortedPartition p12 = p1.multiply(p2);
    // Check result
    assertEquals(p12, expectedP12);
  }

  @Test
  public void testMultiply3() {
    // Setup
    final SortedPartition p1 = new SortedPartition(this.numRows);
    final long[] p1EqC1 = {7, 4, 2};
    final long[] p1EqC2 = {3};
    final long[] p1EqC3 = {6, 5, 1, 0};
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC1));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC2));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC3));

    final SortedPartition p2 = new SortedPartition(this.numRows);
    final long[] p2EqC1 = {4, 2, 7};
    final long[] p2EqC2 = {3, 5};
    final long[] p2EqC3 = {0};
    final long[] p2EqC4 = {6};
    final long[] p2EqC5 = {1};
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC1));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC2));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC3));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC4));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC5));

    // Expected Values
    final SortedPartition expectedP12 = new SortedPartition(this.numRows);
    final long[] p12EqC1 = {2, 4, 7};
    final long[] p12EqC2 = {3};
    final long[] p12EqC3 = {5};
    final long[] p12EqC4 = {0};
    final long[] p12EqC5 = {6};
    final long[] p12EqC6 = {1};
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC1));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC2));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC3));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC4));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC5));
    expectedP12.addEquivalenceClass(new LongOpenHashBigSet(p12EqC6));

    // Execute functionality
    final SortedPartition p12 = p1.multiply(p2);
    // Check result
    assertEquals(p12, expectedP12);
  }

  /**
   * The product of a {@link SortedPartition} A, which represents a unique column permutation, an
   * any other {@link SortedPartition} B, is again, A.
   */
  @Test
  public void testMultiplyUnique() {
    // Setup
    final SortedPartition p1 = new SortedPartition(this.numRows);
    final long[] p1EqC1 = {0};
    final long[] p1EqC2 = {1};
    final long[] p1EqC3 = {2};
    final long[] p1EqC4 = {3};
    final long[] p1EqC5 = {4};
    final long[] p1EqC6 = {5};
    final long[] p1EqC7 = {6};
    final long[] p1EqC8 = {7};
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC1));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC2));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC3));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC4));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC5));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC6));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC7));
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC8));

    final SortedPartition p2 = new SortedPartition(this.numRows);
    final long[] p2EqC1 = {4, 2, 5};
    final long[] p2EqC2 = {0, 7};
    final long[] p2EqC3 = {3, 6, 1};
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC1));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC2));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC3));

    // Expected Values
    // Execute functionality
    // Check result
    assertEquals(p1, p1.multiply(p2));
  }

  /**
   * If there is only one equivalence class in a {@link SortedPartition} A, the product of A and any
   * {@link SortedPartition} B is B.
   */
  @Test
  public void testMultiplyAllEqual() {
    // Setup
    final SortedPartition p1 = new SortedPartition(this.numRows);
    final long[] p1EqC1 = {0, 1, 2, 3, 4, 5, 6, 7};
    p1.addEquivalenceClass(new LongOpenHashBigSet(p1EqC1));

    final SortedPartition p2 = new SortedPartition(this.numRows);
    final long[] p2EqC1 = {4, 2, 5};
    final long[] p2EqC2 = {0, 7};
    final long[] p2EqC3 = {3, 6, 1};
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC1));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC2));
    p2.addEquivalenceClass(new LongOpenHashBigSet(p2EqC3));

    // Expected Values
    // Execute functionality
    // Check result
    assertEquals(p2, p1.multiply(p2));
  }

}
