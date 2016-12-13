package de.hpi.mpss2015n.approxind.inclusiontester;

import com.google.common.base.MoreObjects;
import de.hpi.mpss2015n.approxind.InclusionTester;
import de.hpi.mpss2015n.approxind.datastructures.HyperLogLog;
import de.hpi.mpss2015n.approxind.datastructures.RegisterSet;
import de.hpi.mpss2015n.approxind.utils.ColumnStore;
import de.hpi.mpss2015n.approxind.utils.HLL.HLLData;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * IND test that employs a mixture of HyperLogLog structures and a sample-based inverted index.
 */
public final class HLLInclusionTester extends CombinedInclusionTester<HLLData> {

    /**
     * The desired standard deviation of the relative error of the HLL structures.
     */
    private final double error;

    public HLLInclusionTester(double error) {
        this.error = error;
    }

    @Override
    protected HLLData createApproximateDatastructures(SimpleColumnCombination combination) {
        return new HLLData();
    }

    @Override
    public void finalizeInsertion() {
        super.finalizeInsertion();

        for (Map<SimpleColumnCombination, HLLData> logMap : this.adByTable.values()) {
            for (Entry<SimpleColumnCombination, HLLData> entry : logMap.entrySet()) {
                HLLData hllData = entry.getValue();
                if (hllData.isBig()) {
                    hllData.cacheCardinality(hllData.getHll().cardinality());
                }
            }
        }
    }

    @Override
    protected void insertRowIntoAD(SimpleColumnCombination combination, long hash, HLLData hllData) {
        HyperLogLog hll = hllData.getHll();
        if (hll == null) {
            hll = new HyperLogLog(error);
            hllData.setHll(hll);
        }
        hll.offerHashed(hash);
    }

    @Override
    protected boolean testWithAds(HLLData adA, HLLData adB) {
        if (adA.getCachedCardinality() > adB.getCachedCardinality()) {
            return false;
        }
        return this.isIncluded(adA.getHll(), adB.getHll());
    }

    private int[] getRegisterSetBits(HyperLogLog hll) {
        return hll.registerSet().readOnlyBits();
    }

    //tested to be 25% faster than merging the registerSets
    private boolean isIncluded(HyperLogLog a, HyperLogLog b) {
        int[] aBits = getRegisterSetBits(a);
        int[] bBits = getRegisterSetBits(b);
        for (int bucket = 0; bucket < bBits.length; ++bucket) {
            int aBit = aBits[bucket];
            int bBit = bBits[bucket];
            for (int j = 0; j < RegisterSet.LOG2_BITS_PER_WORD; ++j) {
                int mask = 31 << (RegisterSet.REGISTER_SIZE * j);
                int aVal = aBit & mask;
                int bVal = bBit & mask;
                if (bVal < aVal) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("error", error).toString();
    }

}
