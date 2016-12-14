package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.datastructures.BloomFilter;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;

public final class BloomFilterInclusionTester extends CombinedInclusionTester<BloomFilter> {

    private final int capacityInBytes;

    public BloomFilterInclusionTester(int capacityInBytes) {
        this.capacityInBytes = capacityInBytes;
    }

    @Override
    protected BloomFilter createApproximateDatastructures(SimpleColumnCombination combination) {
        return new BloomFilter(this.capacityInBytes);
    }

    @Override
    protected void insertRowIntoAD(SimpleColumnCombination combination, long hash, BloomFilter bloomFilter) {
        bloomFilter.setHash(hash);
    }

    @Override
    protected boolean testWithAds(BloomFilter bloomFilterA, BloomFilter bloomFilterB) {
        return bloomFilterB.containsAll(bloomFilterA);
    }

}
