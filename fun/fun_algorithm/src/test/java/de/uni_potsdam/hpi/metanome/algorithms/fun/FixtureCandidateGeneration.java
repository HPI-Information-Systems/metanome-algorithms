package de.uni_potsdam.hpi.metanome.algorithms.fun;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
//import static org.mockito.
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;

public class FixtureCandidateGeneration {

    // FIXME key error should be at least 1
    public static final long DEFAULT_COUNT = 1;
    public static final long KEY_COUNT = 0;

    public List<FunQuadruple> getL2() {
        List<FunQuadruple> l2 = new LinkedList<>();

        l2.add(new FunQuadruple(new ColumnCombinationBitset(2, 3), DEFAULT_COUNT));
        l2.add(new FunQuadruple(new ColumnCombinationBitset(1, 2), DEFAULT_COUNT));
        l2.add(new FunQuadruple(new ColumnCombinationBitset(0, 1), DEFAULT_COUNT));
        l2.add(new FunQuadruple(new ColumnCombinationBitset(0, 2), DEFAULT_COUNT));
        l2.add(new FunQuadruple(new ColumnCombinationBitset(5, 6), DEFAULT_COUNT));
        l2.add(new FunQuadruple(new ColumnCombinationBitset(6, 7), KEY_COUNT));
        l2.add(new FunQuadruple(new ColumnCombinationBitset(5, 7), DEFAULT_COUNT));

        return l2;
    }

    public FunAlgorithm getFunAlgorithmMockedAddPliGenerate() {
        FunAlgorithm fun = spy(new FunAlgorithm("table", ImmutableList.of("a", "b"), null));
        PositionListIndex generatedPli = mock(PositionListIndex.class);
        when(generatedPli.getRawKeyError())
                .thenReturn(DEFAULT_COUNT);

        doReturn(generatedPli)
                .when(fun).addPliGenerate(isA(ColumnCombinationBitset.class));

        return fun;
    }

    public FunQuadruple[] getExpectedL2Array() {
        List<FunQuadruple> l2 = getL2();
        return l2.toArray(new FunQuadruple[l2.size()]);
    }

    public FunQuadruple[] getExpectedL3Array() {
        return new FunQuadruple[]{
                new FunQuadruple(new ColumnCombinationBitset(0, 1, 2), DEFAULT_COUNT)};
    }
}
