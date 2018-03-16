package de.metanome.algorithms.cfdfinder.utils;

import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;
import org.apache.lucene.util.OpenBitSet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LhsUtils {

    public static void addSubsetsTo(FDTreeElement.InternalFunctionalDependency fd, Collection<FDTreeElement.InternalFunctionalDependency> collection) {
        List<OpenBitSet> subsets = generateLhsSubsets(fd.lhs);
        for (OpenBitSet subset : subsets) {
            collection.add(new FDTreeElement.InternalFunctionalDependency(subset, fd.rhs));
        }
    }

    public static List<OpenBitSet> generateLhsSubsets(OpenBitSet lhs) {
        List<OpenBitSet> results = new LinkedList<>();
        for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
            OpenBitSet subset = lhs.clone();
            subset.fastFlip(i);
            if (subset.cardinality() > 0) {
                results.add(subset);
            }
        }
        return results;
    }

    public static List<OpenBitSet> generateLhsSupersets(OpenBitSet lhs) {
        List<OpenBitSet> results = new LinkedList<>();
        for (long i = 0; i < lhs.bits().length(); i += 1) {
            if (!lhs.get(i)) {
                OpenBitSet superset = lhs.clone();
                superset.flip(i);
                results.add(superset);
            }
        }
        return results;
    }

}
