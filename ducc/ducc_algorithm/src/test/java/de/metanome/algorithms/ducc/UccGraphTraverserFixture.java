package de.metanome.algorithms.ducc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.*;

public class UccGraphTraverserFixture {
    public List<PositionListIndex> getPLIList() {
        List<PositionListIndex> list = new LinkedList<>();

        PositionListIndex A = new PositionListIndex(getCluster(getLongSet(0, 1, 2)));
        list.add(A);

        PositionListIndex B = new PositionListIndex(getCluster(getLongSet(3, 4)));
        list.add(B);

        PositionListIndex C = new PositionListIndex(getCluster(getLongSet(0, 1, 2, 3)));
        list.add(C);

        PositionListIndex D = new PositionListIndex(getCluster(getLongSet(2, 4)));
        list.add(D);

        PositionListIndex E = new PositionListIndex(getCluster(getLongSet(0, 2)));
        list.add(E);
        return list;
    }

    public Collection<ColumnCombinationBitset> getExpectedBitset() {
        Collection<ColumnCombinationBitset> expectedBitset = new HashSet<>();
        expectedBitset.add(new ColumnCombinationBitset(0, 1));
        expectedBitset.add(new ColumnCombinationBitset(0, 3));
        expectedBitset.add(new ColumnCombinationBitset(1, 2));
        expectedBitset.add(new ColumnCombinationBitset(1, 3));
        expectedBitset.add(new ColumnCombinationBitset(1, 4));
        expectedBitset.add(new ColumnCombinationBitset(2, 3));
        expectedBitset.add(new ColumnCombinationBitset(3, 4));

        return expectedBitset;
    }

    protected LongArrayList getLongSet(long... longs) {
        LongArrayList longSet = new LongArrayList();
        for (long longNumber : longs) {
            longSet.add(longNumber);
        }
        return longSet;
    }

    protected List<LongArrayList> getCluster(LongArrayList... hashSets) {
        List<LongArrayList> cluster = new ArrayList<>();
        Collections.addAll(cluster, hashSets);
        return cluster;
    }
}
