package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public final class CombinedHashSetInclusionTester extends CombinedInclusionTester<LongSet> {

    @Override
    protected LongSet createApproximateDatastructures(SimpleColumnCombination combination) {
        return new LongOpenHashBigSet();
    }

    @Override
    protected void insertRowIntoAD(SimpleColumnCombination combination, long hash, LongSet longSet) {
        longSet.add(hash);
    }

    @Override
    protected boolean testWithAds(LongSet longSet2, LongSet longSet1) {
        return longSet1.containsAll(longSet2);
    }

}
