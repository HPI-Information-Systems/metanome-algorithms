package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.utils.HLL.HLLData;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import de.hpi.mpss2015n.approxind.utils.SimpleInd;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.*;

/**
 * This is an inverted index that maps hash values to the {@link SimpleColumnCombination}s that contain it. However,
 * the keys in this inverted index are restricted to a sample.
 */
public class SampledInvertedIndex {

    private final Long2ObjectOpenHashMap<IntSet> invertedIndex;

    private final Set<SimpleInd> discoveredInds;

    private final BitSet seenColumnComboIndices = new BitSet();

    private final Object2BooleanMap<SimpleColumnCombination> isCovered;

    /**
     * Indices refer to column combinations that appear in INDs. This field keeps track of the maximum possible
     * column combination index (inclusive).
     */
    private int maxIndex;

    public SampledInvertedIndex() {
        invertedIndex = new Long2ObjectOpenHashMap<>();
        discoveredInds = new HashSet<>();
        isCovered = new Object2BooleanOpenHashMap<>();
        isCovered.defaultReturnValue(true);
    }

    public void finalizeInsertion(Collection<? extends Map<SimpleColumnCombination, ?>> hllsByTable) {
        // Initialize a mapping from dependent to referenced column combinations and ind the column combinations.
        Map<SimpleColumnCombination, IntCollection> refByDepColumnCombos = new HashMap<>();
        SimpleColumnCombination[] columnCombinations = new SimpleColumnCombination[maxIndex + 1];
        // Collect all column combinations.
        for (Map<SimpleColumnCombination, ?> hllsByColumnCombo : hllsByTable) {
            for (SimpleColumnCombination scc : hllsByColumnCombo.keySet()) {
                columnCombinations[scc.getIndex()] = scc;
                refByDepColumnCombos.put(scc, null);
            }
        }


        // Apply DeMarchi et al. criterion on all candidate IND simultaneously while iterating over the inverted index.
        for (IntSet valueGroup : invertedIndex.values()) {
            for (IntIterator depIterator = valueGroup.iterator(); depIterator.hasNext(); ) {
                int depColumnCombo = depIterator.nextInt();
                seenColumnComboIndices.set(depColumnCombo);
                IntCollection refColumnCombos = refByDepColumnCombos.get(columnCombinations[depColumnCombo]);

                if (refColumnCombos == null) {
                    // If value is unseen, initialize the referenced column combinations.
                    refColumnCombos = new IntArrayList(valueGroup.size() - 1);
                    for (IntIterator refIterator = valueGroup.iterator(); refIterator.hasNext(); ) {
                        int refColumnCombo = refIterator.nextInt();
                        if (depColumnCombo == refColumnCombo) continue;
                        refColumnCombos.add(refColumnCombo);
                    }
                    refByDepColumnCombos.put(columnCombinations[depColumnCombo], refColumnCombos);

                } else if (!refColumnCombos.isEmpty()) {
                    // Otherwise, intersect referenced values with the values from the inverted index.
                    refColumnCombos.retainAll(valueGroup);
                }
            }
        }
        invertedIndex.clear();

        // Materialize the INDs.
        for (Map.Entry<SimpleColumnCombination, IntCollection> entry : refByDepColumnCombos.entrySet()) {
            SimpleColumnCombination lhs = entry.getKey();
            final IntCollection rhss = entry.getValue();
            if (rhss == null)
                continue; // How can this be? If the value is in the sample, we should observe it at least once.
            for (IntIterator refIter = rhss.iterator(); refIter.hasNext(); ) {
                final int rhs = refIter.nextInt();
                discoveredInds.add(new SimpleInd(lhs, columnCombinations[rhs]));
            }
        }
    }

    public boolean isIncludedIn(SimpleColumnCombination a, SimpleColumnCombination b) {
        return !seenColumnComboIndices.get(a.getIndex()) || discoveredInds.contains(new SimpleInd(a, b));
    }

    public void initialize(List<Long> sampledHashes) {
        // Initialize the inverted index for the given hash values.
        for (Long longHash : sampledHashes) {
            invertedIndex.put(longHash, new IntOpenHashSet(4));
        }
        seenColumnComboIndices.clear();
        discoveredInds.clear();
    }

    /**
     * Try to map the given {@code longHash} to the given {@link SimpleColumnCombination}.
     *
     * @return true if the mapping was successful, which is the case if the {@code longHash} is a valid key in this instance
     */
    public boolean update(SimpleColumnCombination combination, long longHash) {
        IntSet set = invertedIndex.get(longHash);

        if (set == null) {
            this.isCovered.put(combination, false);
            return false;
        }

        set.add(combination.getIndex());
        return true;
    }

    public boolean isCovered(SimpleColumnCombination combination) {
        return this.isCovered.get(combination);
    }

    public void setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
    }
}
