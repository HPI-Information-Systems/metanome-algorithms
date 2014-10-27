package de.uni_potsdam.hpi.metanome.ma2013n2.algorithm_helper.data_structures;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

public class PruningGraph {

    protected final int OVERFLOW_THRESHOLD;
    protected final boolean containsPositiveFeature;

    protected final Map<ColumnCombinationBitset, List<ColumnCombinationBitset>> columnCombinationMap = new HashMap<>();

    protected final List<ColumnCombinationBitset> OVERFLOW = new LinkedList<>();
    protected final int numberOfColumns;
    protected ColumnCombinationBitset allBitsSet;

    public PruningGraph(int numberOfColumns, int overflowTreshold, boolean positiveFeature) {
        this.OVERFLOW_THRESHOLD = overflowTreshold;
        this.numberOfColumns = numberOfColumns;
        this.containsPositiveFeature = positiveFeature;

        int[] setBits = new int[this.numberOfColumns];
        for (int i = 0; i < this.numberOfColumns; i++) {
            setBits[i] = i;
        }
        this.allBitsSet = new ColumnCombinationBitset(setBits);
    }

    /**
     * Adds the given ColumnCombination to the graph. The graph information is automatically pruned and
     * OVERFLOW objects are created when necessary.
     *
     * @param columnCombination
     */
    public void add(ColumnCombinationBitset columnCombination) {
        int initialKeyLength = 1;
        for (int i = 0; i < columnCombination.getNSubsetColumnCombinations(initialKeyLength).size(); i++) {
            this.addToKey(columnCombination.getNSubsetColumnCombinations(initialKeyLength).get(i), columnCombination, initialKeyLength);
        }
    }

    protected void addToKey(ColumnCombinationBitset key, ColumnCombinationBitset columnCombination, int keyLength) {
        List<ColumnCombinationBitset> columnCombinationList = this.columnCombinationMap.get(key);

        // create new set for given key
        if (null == columnCombinationList) {
            columnCombinationList = new LinkedList<>();
            columnCombinationList.add(columnCombination);
            //TODO check if clone() is needed
            this.columnCombinationMap.put(key, columnCombinationList);

            //find correct set when current set is OVERFLOW
        } else if (OVERFLOW == columnCombinationList) {
            this.addToSubKey(key, columnCombination, keyLength + 1);

            // add current columnCombination to the list and prune the information in the graph
        } else if (!columnCombinationList.contains(columnCombination)) {
            Iterator<ColumnCombinationBitset> iterator = columnCombinationList.iterator();
            while (iterator.hasNext()) {
                ColumnCombinationBitset currentBitSet = iterator.next();
                if (this.containsPositiveFeature) {
                    if (columnCombination.containsSubset(currentBitSet)) {
                        return;
                    } else if (columnCombination.isSubsetOf(currentBitSet)) {
                        iterator.remove();
                    }
                } else {
                    if (columnCombination.isSubsetOf(currentBitSet)) {
                        return;
                    } else if (columnCombination.containsSubset(currentBitSet)) {
                        iterator.remove();
                    }
                }
            }
            columnCombinationList.add(columnCombination);

            // create OVERFLOW elements in the hashmap
            if (columnCombinationList.size() >= OVERFLOW_THRESHOLD) {
                //TODO check if clone() is needed
                this.columnCombinationMap.put(key, OVERFLOW);
                for (ColumnCombinationBitset subCombination : columnCombinationList) {
                    this.addToSubKey(key, subCombination, keyLength + 1);
                }
            }
        }
    }

    protected void addToSubKey(ColumnCombinationBitset key, ColumnCombinationBitset columnCombination, int keyLength) {
        for (int i = 0; i < columnCombination.getNSubsetColumnCombinations(keyLength).size(); i++) {
            this.addToKey(columnCombination.getNSubsetColumnCombinations(keyLength).get(i), columnCombination, keyLength);
        }
    }

    /**
     * Returns true iff the given columnCombination is a subset(negative graph) or superset(positive graph).
     * If true is returned the given columnCombination is already pruned.
     *
     * @param columnCombination
     * @return
     */
    public boolean find(ColumnCombinationBitset columnCombination) {
        return findRecursively(columnCombination, new ColumnCombinationBitset(), 1);
    }

    /**
     * Protected method used by {@link #find(ColumnCombinationBitset)}
     *
     * @param columnCombination
     * @param subset
     * @param n
     * @return
     */
    protected boolean findRecursively(ColumnCombinationBitset columnCombination, ColumnCombinationBitset subset, int n) {
        List<ColumnCombinationBitset> currentColumnCombinations;
        List<ColumnCombinationBitset> keySetList;
        if (columnCombination.size() <= n) {
            keySetList = allBitsSet.getNSubsetColumnCombinationsSupersetOf(columnCombination, n);
        } else {
            keySetList = columnCombination.getNSubsetColumnCombinationsSupersetOf(subset, n);
        }
        for (ColumnCombinationBitset keySet : keySetList) {
            currentColumnCombinations = this.columnCombinationMap.get(keySet);
            if ((currentColumnCombinations != OVERFLOW) && (currentColumnCombinations != null)) {
                for (ColumnCombinationBitset currentColumnBitset : currentColumnCombinations) {
                    if (this.containsPositiveFeature) {
                        if (columnCombination.containsSubset(currentColumnBitset)) {
                            return true;
                        }
                    } else {
                        if (columnCombination.isSubsetOf(currentColumnBitset)) {
                            return true;
                        }
                    }
                }
            } else if (OVERFLOW == currentColumnCombinations) {
                if (findRecursively(columnCombination, keySet, n + 1)) {
                    return true;
                }
            }
        }
        return false;
    }
}
