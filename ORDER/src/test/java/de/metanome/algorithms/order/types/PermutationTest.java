package de.metanome.algorithms.order.types;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PermutationTest {

  @Test
  public void testSubPermutations() {

    // Setup
    final int[] permutationIndices = {0, 1, 2, 3, 4};
    final Permutation permutation = new Permutation(permutationIndices, 5);

    // Expected values
    final List<Permutation> expectedSubPermutations = new ArrayList<Permutation>();
    final int[] expectedPermutationIndices1 = {1, 2, 3, 4};
    final int[] expectedPermutationIndices2 = {0, 2, 3, 4};
    final int[] expectedPermutationIndices3 = {0, 1, 3, 4};
    final int[] expectedPermutationIndices4 = {0, 1, 2, 4};
    final int[] expectedPermutationIndices5 = {0, 1, 2, 3};
    final Permutation expectedPermutation1 = new Permutation(expectedPermutationIndices1, 5);
    final Permutation expectedPermutation2 = new Permutation(expectedPermutationIndices2, 5);
    final Permutation expectedPermutation3 = new Permutation(expectedPermutationIndices3, 5);
    final Permutation expectedPermutation4 = new Permutation(expectedPermutationIndices4, 5);
    final Permutation expectedPermutation5 = new Permutation(expectedPermutationIndices5, 5);

    expectedSubPermutations.add(expectedPermutation1);
    expectedSubPermutations.add(expectedPermutation2);
    expectedSubPermutations.add(expectedPermutation3);
    expectedSubPermutations.add(expectedPermutation4);
    expectedSubPermutations.add(expectedPermutation5);
    // Execute functionality
    final List<Permutation> actualSubPermutations = permutation.subPermutations();
    // Check result
    assertEquals(actualSubPermutations, expectedSubPermutations);
  }

}
