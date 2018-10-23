package de.metanome.algorithms.cfdfinder.utils;

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;

public class LhsUtils {

    public static void addSubsetsTo(FDTreeElement.InternalFunctionalDependency fd, Collection<FDTreeElement.InternalFunctionalDependency> collection) {
        List<BitSet> subsets = generateLhsSubsets(fd.lhs);
        for (BitSet subset : subsets) {
            collection.add(new FDTreeElement.InternalFunctionalDependency(subset, fd.rhs, fd.numAttributes));
        }
    }

    public static List<BitSet> generateLhsSubsets(BitSet lhs) {
        List<BitSet> results = new LinkedList<>();
        for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
            BitSet subset = (BitSet) lhs.clone();
            subset.flip(i);
            if (subset.cardinality() > 0) {
                results.add(subset);
            }
        }
        return results;
    }

    public static List<BitSet> generateLhsSupersets(BitSet lhs, int numAttributes) {
        List<BitSet> results = new LinkedList<>();
        for (int i = 0; i < numAttributes; i += 1) {
            if (!lhs.get(i)) {
                BitSet superset = (BitSet) lhs.clone();
                superset.set(i);
                results.add(superset);
            }
        }
        return results;
    }

}
