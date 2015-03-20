package de.metanome.algorithms.order.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ByteArrayPermutationsTest {

  @Test
  public void testJoin() {
    // Setup
    final byte[] first = {0, 1, 2};
    final byte[] second = {0, 1, 3};
    // Expected values
    final byte[] expectedJoinedPermutation = {0, 1, 2, 3};
    // Execute functionality
    final byte[] actualJoinedPermutation = ByteArrayPermutations.join(first, second);
    // Compare result
    assertTrue(Arrays.equals(actualJoinedPermutation, expectedJoinedPermutation));
  }

  @Test
  public void testDisjoint() {
    // Setup
    final byte[] first = {5, 6, 2, 7, 8};
    final byte[] second = {0, 1, 2, 3, 4};
    final byte[] third = {10, 11, 12};
    // Expected values
    // Execute functionality
    // Compare result
    assertFalse(ByteArrayPermutations.disjoint(first, second));
    assertFalse(ByteArrayPermutations.disjoint(second, first));
    assertTrue(ByteArrayPermutations.disjoint(first, third));
    assertTrue(ByteArrayPermutations.disjoint(second, third));
  }

  @Test
  public void testToString() {
    // Setup
    final byte[] permutation = {5, 6, 2, 7, 8};
    // Expected values
    final String expectedBinaryString = "[00000101, 00000110, 00000010, 00000111, 00001000]";
    final String expectedIntegerString = "[5, 6, 2, 7, 8]";
    // Execute functionality
    final String actualBinaryString = ByteArrayPermutations.permutationToBinaryString(permutation);
    final String actualIntegerString =
        ByteArrayPermutations.permutationToIntegerString(permutation);
    // Compare result
    assertEquals(actualBinaryString, expectedBinaryString);
    assertEquals(actualIntegerString, expectedIntegerString);
  }

  @Test
  public void testPrefix() {
    // Setup
    final byte[] permutation = {5, 6, 2, 7, 8};
    // Expected values
    final byte[] expectedPrefix = {5, 6, 2, 7};
    // Execute functionality
    final byte[] actualPrefix = ByteArrayPermutations.prefix(permutation);
    // Compare result
    assertTrue(Arrays.equals(expectedPrefix, actualPrefix));
  }

  @Test
  public void testPrefixes() {
    // Setup
    final byte[] permutation = {1, 2, 3, 4};
    // Expected values
    final byte[][] expectedPrefixes = { {1, 2, 3}, {1, 2}, {1}};
    // Execute functionality
    final byte[][] actualPrefixes = ByteArrayPermutations.prefixes(permutation);
    // Compare result
    for (int i = 0; i < expectedPrefixes.length; i++) {
      assertTrue(Arrays.equals(expectedPrefixes[i], actualPrefixes[i]));
    }
  }

  @Test
  public void testCustomHashDataStructuresForByteArrays() {
    final Set<byte[]> customHashSet1 =
        new ObjectOpenCustomHashSet<byte[]>(ByteArrays.HASH_STRATEGY);


        final Set<byte[]> customHashSet2 =
            new ObjectOpenCustomHashSet<byte[]>(ByteArrays.HASH_STRATEGY);
            final Map<byte[], Set<byte[]>> customHashMap =
                new Object2ObjectOpenCustomHashMap<byte[], Set<byte[]>>(ByteArrays.HASH_STRATEGY);

            final byte[] b1 = {1, 2, 3, 4};
            final byte[] b2 = {4, 8, 9, 12};
            final byte[] b3 = {1, 2, 3, 5};
            final byte[] b4 = {4, 8, 9, 12};

            customHashSet1.add(b1);
            customHashSet1.add(b2);
            customHashSet1.add(b3);
            customHashSet1.add(b4);

            customHashSet2.add(b3);
            customHashSet2.add(b1);
            customHashSet2.add(b2);
            customHashSet2.add(b4);

            assertEquals(3, customHashSet1.size());
            assertEquals(3, customHashSet2.size());

            customHashMap.put(b1, customHashSet1);
            customHashMap.put(b2, customHashSet2);

            assertEquals(2, customHashMap.keySet().size());

            customHashMap.put(b4, customHashSet2);

            assertEquals(2, customHashMap.keySet().size());

  }

  @Test
  public void testSubPermutations() {
    // Setup
    final byte[] permutation = {1, 2, 3, 4, 5};
    // Expected values
    final byte[][] expectedSubPermutations =
        { {1, 2, 3, 4}, {1, 2, 3, 5}, {1, 2, 4, 5}, {1, 3, 4, 5}, {2, 3, 4, 5}};
    // Execute functionality
    final byte[][] actualSubPermutations = ByteArrayPermutations.subPermutations(permutation);
    // Compare result
    for (int i = 0; i < expectedSubPermutations.length; i++) {
      assertTrue(Arrays.equals(actualSubPermutations[i], expectedSubPermutations[i]));
    }
  }

  @Test
  public void testSuffix() {
    // Setup
    final byte[] permutation = {1, 2, 3, 4, 5};
    // Expected values
    final byte[] expectedSuffix = {5};
    // Execute functionality
    final byte[] actualSuffix = ByteArrayPermutations.suffix(permutation);
    // Compare result
    assertTrue(Arrays.equals(expectedSuffix, actualSuffix));
  }

  @Test
  public void testFindOccurrenceOfSingle() {
    // Setup
    final byte[] first = {1, 7, 2, 9, 4};
    final byte[] second = {2};
    // Expected values
    final int expectedIndex = 3;
    // Execute functionality
    final int actualIndex = ByteArrayPermutations.findOccurrenceOf(second, first, 0);
    // Compare result
    assertEquals(expectedIndex, actualIndex);
  }

  @Test
  public void testFindOccurrenceOfMulti() {
    // Setup
    final byte[] first = {1, 7, 2, 9, 4};
    final byte[] second = {7, 2, 9};
    // Expected values
    final int expectedIndex = 4;
    // Execute functionality
    final int actualIndex = ByteArrayPermutations.findOccurrenceOf(second, first, 0);
    // Compare result
    assertEquals(expectedIndex, actualIndex);
  }

  @Test
  public void testFindOccurrenceOfMultiAll() {
    // Setup
    final byte[] first = {1, 7, 2, 9, 4};
    final byte[] second = {1, 7, 2, 9, 4};
    // Expected values
    final int expectedIndex = 5;
    // Execute functionality
    final int actualIndex = ByteArrayPermutations.findOccurrenceOf(second, first, 0);
    // Compare result
    assertEquals(expectedIndex, actualIndex);
  }

  @Test
  public void testFindOccurrenceOfMultiBegin() {
    // Setup
    final byte[] first = {1, 7, 2, 9, 4};
    final byte[] second = {1, 7};
    // Expected values
    final int expectedIndex = 2;
    // Execute functionality
    final int actualIndex = ByteArrayPermutations.findOccurrenceOf(second, first, 0);
    // Compare result
    assertEquals(expectedIndex, actualIndex);
  }

  @Test
  public void testFindOccurrenceOfMultiEnd() {
    // Setup
    final byte[] first = {1, 7, 2, 9, 4};
    final byte[] second = {2, 9, 4};
    // Expected values
    final int expectedIndex = 5;
    // Execute functionality
    final int actualIndex = ByteArrayPermutations.findOccurrenceOf(second, first, 0);
    // Compare result
    assertEquals(expectedIndex, actualIndex);
  }

  @Test
  public void testFindOccurrenceOfSingleFrom() {
    // Setup
    final byte[] first = {1, 7, 2, 9, 4};
    final byte[] second = {9};
    // Expected values
    final int expectedIndex = 4;
    // Execute functionality
    final int actualIndex = ByteArrayPermutations.findOccurrenceOf(second, first, 2);
    // Compare result
    assertEquals(expectedIndex, actualIndex);
  }

  @Test
  public void testFindOccurrenceOfMultiFromNotFound() {
    // Setup
    final byte[] first = {1, 7, 2, 9, 4};
    final byte[] second = {2};
    // Expected values
    final int expectedIndex = -1;
    // Execute functionality
    final int actualIndex = ByteArrayPermutations.findOccurrenceOf(second, first, 3);
    // Compare result
    assertEquals(expectedIndex, actualIndex);
  }

  @Test
  public void testFindOccurrenceOfIndexTooLarge() {
    // Setup
    final byte[] first = {1, 7, 2, 9, 4};
    final byte[] second = {1, 7, 2, 9, 4};
    // Expected values
    final int expectedIndex = -1;
    // Execute functionality
    final int actualIndex = ByteArrayPermutations.findOccurrenceOf(second, first, 1);
    // Compare result
    assertEquals(expectedIndex, actualIndex);
  }
}
