package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.BitVectorFactory;
import de.hpi.mpss2015n.approxind.datastructures.bloomfilter.BloomFilter;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

import java.util.HashMap;
import java.util.Map;

public final class BloomFilterInclusionTester extends CombinedInclusionTester<BloomFilter<Long>> {

    private final BitVectorFactory bitVectorFactory;

    public BloomFilterInclusionTester() {
        this.bitVectorFactory = new BitVectorFactory(true); // assuming TRUE is default
    }

    @Override
    protected BloomFilter<Long> createApproximateDatastructures(SimpleColumnCombination combination) {
        return new BloomFilter<>(32, 2, bitVectorFactory);
    }

    @Override
    protected void insertRowIntoAD(SimpleColumnCombination combination, long hash, BloomFilter<Long> bloomFilter) {
        bloomFilter.add(new byte[]{
                (byte) (hash >> 56),
                (byte) (hash >> 48),
                (byte) (hash >> 40),
                (byte) (hash >> 32),
                (byte) (hash >> 24),
                (byte) (hash >> 16),
                (byte) (hash >> 8),
                (byte) hash
        });
    }

    @Override
    protected boolean testWithAds(BloomFilter<Long> bloomFilterA, BloomFilter<Long> bloomFilterB) {
        long[] bitsA = bloomFilterA.getBits().getBits();
        long[] bitsB = bloomFilterB.getBits().getBits();

        for (int i = 0; i < bitsA.length; i++) {
            long a = bitsA[i];
            long b = bitsB[i];
            if ((a & ~b) != 0) return false;
        }

        return true;
    }

}
