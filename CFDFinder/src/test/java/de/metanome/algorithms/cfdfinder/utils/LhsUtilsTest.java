package de.metanome.algorithms.cfdfinder.utils;

import java.util.BitSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LhsUtilsTest {

    @Test
    public void testGenerateLhsSubsetsDoesNotReturnAnythingIfThereAreNoSubsets() {
        List<BitSet> results = LhsUtils.generateLhsSubsets(new BitSet(64));
        Assert.assertEquals(0, results.size());
    }

    @Test
    public void testGenerateLhsSubsetsDoesGenerateSetsWithOneLessAttribute() {
        BitSet parent = generateLhs(64, new int[] {1, 2, 3});
        List<BitSet> results = LhsUtils.generateLhsSubsets(parent);
        Assert.assertEquals(3, results.size());
        Assert.assertTrue(results.contains(generateLhs(64, new int[] {1, 2})));
        Assert.assertTrue(results.contains(generateLhs(64, new int[] {2, 3})));
        Assert.assertTrue(results.contains(generateLhs(64, new int[] {1, 3})));
    }

    @Test
    public void testGenerateLhsSupersetsDoesNotReturnAnythingIfAllAttributesAreAlreadyThere() {
       List<BitSet> results = LhsUtils.generateLhsSupersets(generateLhs(64, new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
            30, 31, 32, 33, 34 ,35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56,
            57, 58, 59, 60, 61, 62, 63
        }), 64);
        Assert.assertEquals(0, results.size());
    }

    @Test
    public void testGenerateLhsSupersetsDoesGenerateSetsWithOneAdditionalAttribute() {
        BitSet expected = generateLhs(64, new int[] {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 31, 32, 33, 34 ,35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56,
                57, 58, 59, 60, 61, 62, 63
        });
        List<BitSet> results = LhsUtils.generateLhsSupersets(generateLhs(64, new int[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
            30, 31, 32, 33, 34 ,35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56,
            57, 58, 59, 60, 61, 62, 63
        }), 64);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(expected, results.get(0));
    }

    private BitSet generateLhs(int size, int[] setBits) {
        BitSet result = new BitSet(size);
        for (int bit : setBits) {
            result.flip(bit);
        }
        return result;
    }

}
