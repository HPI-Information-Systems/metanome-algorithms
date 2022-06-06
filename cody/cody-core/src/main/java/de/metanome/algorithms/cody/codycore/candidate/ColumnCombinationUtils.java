package de.metanome.algorithms.cody.codycore.candidate;

import ch.javasoft.bitset.LongBitSet;
import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@UtilityClass
public class ColumnCombinationUtils {

    /**
     * Generate all ColumnCombinations with c's cardinality - 1 that are also subsets of c
     *
     * @param c CheckedColumnCombination the parent
     * @return a list of ColumnCombinations
     */
    public List<ColumnCombination> getImmediateSubsets(CheckedColumnCombination c) {
        List<ColumnCombination> result = new ArrayList<>(c.getColumns().cardinality());

        if (c.getLeft().cardinality() > 1) {
            for (int i = c.getLeft().nextSetBit(0); i != -1; i = c.getLeft().nextSetBit(i + 1)) {
                LongBitSet newLeft = c.getLeft().clone();
                newLeft.clear(i);
                result.add(new ColumnCombination(newLeft, c.getRight()));
            }
        }

        if (c.getRight().cardinality() > 1) {
            for (int i = c.getRight().nextSetBit(0); i != -1; i = c.getRight().nextSetBit(i + 1)) {
                LongBitSet newRight = c.getRight().clone();
                newRight.clear(i);
                result.add(new ColumnCombination(c.getLeft(), newRight));
            }
        }

        return ImmutableList.copyOf(result);
    }

    public CheckedColumnCombination inflateDuplicateColumns(CheckedColumnCombination c,
                                        List<List<Integer>> columnIndexToDuplicatesMapping) {
        LongBitSet inflatedLeft = new LongBitSet();
        for (int i = c.getLeft().nextSetBit(0); i != -1; i = c.getLeft().nextSetBit(i + 1)) {
            for (int j : columnIndexToDuplicatesMapping.get(i))
                inflatedLeft.set(j);
        }

        LongBitSet inflatedRight = new LongBitSet();
        for (int i = c.getRight().nextSetBit(0); i != -1; i = c.getRight().nextSetBit(i + 1)) {
            for (int j : columnIndexToDuplicatesMapping.get(i))
                inflatedRight.set(j);
        }

        return new CheckedColumnCombination(new ColumnCombination(inflatedLeft, inflatedRight), c.getSupport());
    }
}
