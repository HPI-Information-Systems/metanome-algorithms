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
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * IND test that employs a mixture of HyperLogLog structures and a sample-based inverted index.
 */
public final class HLLInclusionTester implements InclusionTester {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Map table IDs to the HLL structures of their column combinations.
     */
    private final Int2ObjectMap<Map<SimpleColumnCombination, HLLData>> hllsByTable;

    /**
     * Caches cardinalities of column combinations.
     */
    private final Object2LongMap<SimpleColumnCombination> cardinalityLookUp;

    /**
     * The desired standard deviation of the relative error of the HLL structures.
     */
    private final double error;

    /**
     * ???
     */
    private final int demarchiThreshold = 10000;

    /**
     * Nested IND test via an inverted index as proposed by DeMarchi et al.
     */
    private final DeMarchi deMarchi;

    /**
     * ???
     */
    private Map.Entry<SimpleColumnCombination, HLLData>[] insertHelper;

    public HLLInclusionTester(double error) {
        this.error = error;
        this.hllsByTable = new Int2ObjectOpenHashMap<>();
        cardinalityLookUp = new Object2LongOpenHashMap<>();
        deMarchi = new DeMarchi(demarchiThreshold);
    }

    @Override
    public void initialize(List<List<long[]>> tableSamplesList) {
        // Collect all column combinations that can be inserted.
        List<SimpleColumnCombination> combinations = new ArrayList<>();
        for (Map<SimpleColumnCombination, HLLData> hllsByColumnCombo : hllsByTable.values()) {
            combinations.addAll(hllsByColumnCombo.keySet());
        }
        for (int i = 0; i < combinations.size(); i++) {
            combinations.get(i).setIndex(i);
        }
        deMarchi.setMaxIndex(combinations.size() - 1);

        // Now create according hashes from the table samples and initialize the inverted index with them.
        List<Long> samples = new ArrayList<>();
        for (int table = 0; table < tableSamplesList.size(); table++) {
            Map<SimpleColumnCombination, HLLData> hllsByColumnCombo = hllsByTable.get(table);
            if (hllsByColumnCombo != null) {
                List<long[]> tableSamples = tableSamplesList.get(table);
                for (long[] sampleRow : tableSamples) {
                    for (Map.Entry<SimpleColumnCombination, HLLData> entry : hllsByColumnCombo.entrySet()) {
                        SimpleColumnCombination combination = entry.getKey();
                        Long combinedHash = getHash(combination, sampleRow);
                        if (combinedHash != null) {
                            samples.add(combinedHash);
                        }
                    }
                }
            }
        }
        deMarchi.initialize(samples);
    }

    @Override
    public int[] setColumnCombinations(List<SimpleColumnCombination> combinations) {
        hllsByTable.clear();
        int[]
                activeTables =
                combinations.stream().mapToInt(SimpleColumnCombination::getTable).distinct().sorted()
                        .toArray();
        for (int table : activeTables) {
            hllsByTable.put(table, new HashMap<>());
        }
        for (SimpleColumnCombination combination : combinations) {
            hllsByTable.get(combination.getTable()).put(combination, new HLLData());
        }
        return activeTables;
    }

    @Override
    public void finalizeInsertion() {
        for (Map<SimpleColumnCombination, HLLData> logMap : hllsByTable.values()) {
            for (Map.Entry<SimpleColumnCombination, HLLData> entry : logMap.entrySet()) {
                if (entry.getValue().isBig()) {
                    cardinalityLookUp.put(entry.getKey(), entry.getValue().getHll().cardinality());
                }
            }
        }
        deMarchi.finalizeInsertion(hllsByTable.values());
    }

    @Override
    public void insertRow(long[] values, int rowCount) {

        for (Map.Entry<SimpleColumnCombination, HLLData> entry : insertHelper) {
            SimpleColumnCombination combination = entry.getKey();
            HLLData hllData = entry.getValue();
            Long combinedHash = getHash(combination, values);
            if (combinedHash != null) {
                processHash(combination, hllData, combinedHash);
            }
        }

    }

    /**
     * Creates a combined hash from the existing {@code values} according to the {@link SimpleColumnCombination}.
     *
     * @return the hash value or {@code null} if any of the input {@code values} is the {@link ColumnStore#NULLHASH}
     */
    public Long getHash(SimpleColumnCombination combination, long[] values) {
        long combinedHash = 0;
        int[] columns = combination.getColumns();
        for (int i = 0; i < columns.length; i++) {
            long hash = values[columns[i]];
            if (hash == ColumnStore.NULLHASH) return null;
            combinedHash = combinedHash * 37 ^ hash;
        }
        return combinedHash;
    }

    @Override
    public boolean isIncludedIn(SimpleColumnCombination a, SimpleColumnCombination b) {
        //In case combination was removes - Todo: test if ind is valid based on generated ind cover
        if (!hllsByTable.get(a.getTable()).containsKey(a) || !hllsByTable.get(b.getTable()).containsKey(b)) {
            return false;
        }

        HLLData dataA = hllsByTable.get(a.getTable()).get(a);
        HLLData dataB = hllsByTable.get(b.getTable()).get(b);
    /*if (a.getTable() == 7 && a.getColumns()[0] == 4){
      System.out.println(dataA.isBig() + ": " + cardinalityLookUp.get(a) + a.toString());
    }*/
        if (dataA.isBig() && dataB.isBig()) {
            //great performance improvement using look up
            //first test indicate at least around factor 20 speed up
            // due to saving potentially billions of calls to registerSet.get() from HyperLogLog.cardinality!
            long setACardinality = cardinalityLookUp.getLong(a);
            long setBCardinality = cardinalityLookUp.getLong(b);
      /*
        if(setBCardinality > 0 && setACardinality > 0 && error > setACardinality/(double) setBCardinality){
          logger.info("No sufficient statement can be made due to difference in size");
          return false;
        }
      */
            if (setACardinality > setBCardinality) {
                return false;
            }

            return deMarchi.isIncludedIn(a, b) && isIncluded(dataA.getHll(), dataB.getHll());

        } else {
            return deMarchi.isIncludedIn(a, b);
        }
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

    private void processHash(SimpleColumnCombination combination, HLLData hllData, long longHash) {
        boolean exists = deMarchi.processHash(combination, hllData, longHash);
        if (!exists && hllData.isBig()) {
            HyperLogLog hll = hllData.getHll();
            if (hll == null) {
                hll = new HyperLogLog(error);
                ;
                hllData.setHll(hll);
            }
            hll.offerHashed(longHash);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void startInsertRow(int table) {
        Set<Entry<SimpleColumnCombination, HLLData>> set = hllsByTable.get(table).entrySet();
        insertHelper = set.toArray(new Entry[set.size()]);
    }

}
