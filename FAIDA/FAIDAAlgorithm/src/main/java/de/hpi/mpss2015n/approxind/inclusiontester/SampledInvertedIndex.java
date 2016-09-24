package de.hpi.mpss2015n.approxind.inclusiontester;

import de.hpi.mpss2015n.approxind.utils.HLL.HLLData;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import de.hpi.mpss2015n.approxind.utils.SimpleInd;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;
import java.util.stream.Stream;

/**
 * This is an inverted index that maps hash values to the {@link SimpleColumnCombination}s that contain it. However,
 * the keys in this inverted index are restricted to a sample.
 */
public class SampledInvertedIndex {

    private final Long2ObjectOpenHashMap<BitSet> invertedIndex;

    private final Set<SimpleInd> discoveredInds;
    /**
     * Indices refer to column combinations that appear in INDs. This field keeps track of the maximum possible
     * column combination index (inclusive).
     */
    private int maxIndex;

    public SampledInvertedIndex() {
        invertedIndex = new Long2ObjectOpenHashMap<>();
        discoveredInds = new HashSet<>();
    }

    public void finalizeInsertion(Collection<Map<SimpleColumnCombination, HLLData>> hllsByTable) {
        // Initialize a mapping from dependent to referenced column combinations and ind the column combinations.
        Map<SimpleColumnCombination, IntCollection> refByDepColumnCombos = new HashMap<>();
        SimpleColumnCombination[] columnCombinations = new SimpleColumnCombination[maxIndex + 1];
        // Collect all column combinations.
        for (Map<SimpleColumnCombination, HLLData> hllsByColumnCombo : hllsByTable) {
            for (SimpleColumnCombination scc : hllsByColumnCombo.keySet()) {
                columnCombinations[scc.getIndex()] = scc;
                refByDepColumnCombos.put(scc, null);
            }
        }

        // Apply DeMarchi et al. criterion on all candidate IND simultaneously while iterating over the inverted index.
        for (BitSet valueGroup : invertedIndex.values()) {
            for (int depColumnCombo = valueGroup.nextSetBit(0); depColumnCombo != -1; depColumnCombo = valueGroup.nextSetBit(depColumnCombo + 1)) {
                IntCollection refColumnCombos = refByDepColumnCombos.get(columnCombinations[depColumnCombo]);

                if (refColumnCombos == null) {
                    // If value is unseen, initialize the referenced column combinations.
                    refColumnCombos = new IntArrayList(valueGroup.cardinality() - 1);
                    for (int refColumnCombo = valueGroup.nextSetBit(0); refColumnCombo != -1; refColumnCombo = valueGroup.nextSetBit(refColumnCombo + 1)) {
                        if (depColumnCombo == refColumnCombo) continue;
                        refColumnCombos.add(refColumnCombo);
                    }
                    refByDepColumnCombos.put(columnCombinations[depColumnCombo], refColumnCombos);

                } else if (!refColumnCombos.isEmpty()) {
                    // Otherwise, intersect referenced values with the values from the inverted index.
                    for (IntIterator refIter = refColumnCombos.iterator(); refIter.hasNext();) {
                        final int refColumnCombo = refIter.nextInt();
                        if (!valueGroup.get(refColumnCombo)) refIter.remove();
                    }
                }
            }
        }
        invertedIndex.clear();

        // Materialize the INDs.
        for (Map.Entry<SimpleColumnCombination, IntCollection> entry : refByDepColumnCombos.entrySet()) {
            SimpleColumnCombination lhs = entry.getKey();
            final IntCollection rhss = entry.getValue();
            if (rhss == null) continue; // How can this be? If the value is in the sample, we should observe it at least once.
            for (IntIterator refIter = rhss.iterator(); refIter.hasNext();) {
                final int rhs = refIter.nextInt();
                discoveredInds.add(new SimpleInd(lhs, columnCombinations[rhs]));
            }
        }
    }

    public boolean isIncludedIn(SimpleColumnCombination a, SimpleColumnCombination b) {
        return discoveredInds.contains(new SimpleInd(a, b));
    }

    public void initialize(List<Long> sampledHashes) {
        // Initialize the inverted index for the given hash values.
        for (Long longHash : sampledHashes) {
            BitSet set = new BitSet(maxIndex + 1);
            invertedIndex.put(longHash, set);
        }
        discoveredInds.clear();
    }

    /**
     * Try to map the given {@code longHash} to the given {@link SimpleColumnCombination}.
     *
     * @return true if the mapping was successful, which is the case if the {@code longHash} is a valid key in this instance
     */
    public boolean update(SimpleColumnCombination combination, HLLData hllData, long longHash) {
        BitSet set = invertedIndex.get(longHash);

        if (set == null) {
            hllData.setBig(true);
            return false;
        }

        set.set(combination.getIndex());
        return true;

    }

    public void setMaxIndex(int maxIndex) {
        this.maxIndex = maxIndex;
    }
}
