package de.metanome.algorithms.anelosimus.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import de.metanome.algorithms.anelosimus.bitvectors.BitVectorFactory;
import de.metanome.algorithms.anelosimus.bloom_filtering.BloomFilter;

public class BloomFilterTest {
    private BitVectorFactory bitVectorFactory = new BitVectorFactory(true);

    @Test
    public void testBloomFilter() {
        BloomFilter<Integer> filter1 = new BloomFilter<>(10, 2, bitVectorFactory);
        filter1.add(5);
        assertTrue(filter1.contains(5));

        assertFalse(filter1.contains(4));
    }

    @Test
    public void testBloomFilterWithPAndN() {
        BloomFilter<String> filter1 = new BloomFilter<>(0.00001, 100, bitVectorFactory);
        filter1.add("");
        assertTrue(filter1.contains(""));
    }

    @Test
    public void testBloomFilterNULLAdd() {
        BloomFilter<Integer> filter1 = new BloomFilter<>(10, 2, bitVectorFactory);
        filter1.add(null);
        for (int i = 0; i < filter1.getBits().size(); i++) {
            assertFalse(filter1.getBits().get(i));
        }
    }

    @Test
    public void testHugeBloomFilter() {
        BloomFilter<String> filter1 = new BloomFilter<>(1000000, 3, bitVectorFactory);
        for (int i = 0; i < 100000; i++) {
            String s = RandomStringUtils.randomAlphabetic(i % 25 + 1);
            //System.out.println(s);
            filter1.add(s);
        }
        // TODO check normal distribution
    }

    @Test
    public void testSalt() {
        BloomFilter<String> filter1 = new BloomFilter<String>(
                128, 3, bitVectorFactory, (byte) 0);
        BloomFilter<String> filter2 = new BloomFilter<String>(
                128, 3, bitVectorFactory, (byte) 1);
        // byte overflow 256 -> 0
        BloomFilter<String> filter3 = new BloomFilter<String>(
                128, 3, bitVectorFactory, (byte) 256);
        filter1.add("foo");
        filter2.add("foo");
        filter3.add("foo");
        assertNotEquals(filter1, filter2);
        assertEquals(filter1, filter3);
    }
}
