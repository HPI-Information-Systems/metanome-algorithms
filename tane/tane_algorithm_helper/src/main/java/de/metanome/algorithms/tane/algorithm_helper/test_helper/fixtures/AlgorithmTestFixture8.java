package de.metanome.algorithms.tane.algorithm_helper.test_helper.fixtures;

import com.google.common.collect.ImmutableList;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.FunctionalDependency;

import static org.mockito.Mockito.*;

public class AlgorithmTestFixture8 extends AbstractAlgorithmTestFixture {

    public AlgorithmTestFixture8() throws CouldNotReceiveResultException {
        columnNames = ImmutableList.of("A", "B", "C", "D");
        numberOfColumns = 4;
        table.add(ImmutableList.of("197", "145", "101", "171"));
        table.add(ImmutableList.of("57", "70", "145", "112"));
        table.add(ImmutableList.of("197", "70", "130", "39"));
        table.add(ImmutableList.of("122", "10", "137", "15"));
        table.add(ImmutableList.of("113", "72", "18", "124"));
    }

    public RelationalInput getRelationalInput() throws InputIterationException {
        RelationalInput input = mock(RelationalInput.class);

        when(input.columnNames())
                .thenReturn(this.columnNames);
        when(input.numberOfColumns())
                .thenReturn(this.numberOfColumns);
        when(input.relationName())
                .thenReturn(this.relationName);

        when(input.hasNext())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(input.next())
                .thenReturn(this.table.get(0))
                .thenReturn(this.table.get(1))
                .thenReturn(this.table.get(2))
                .thenReturn(this.table.get(3))
                .thenReturn(this.table.get(4));

        return input;
    }

    public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException {
        ColumnIdentifier expectedIdentifierA = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
        ColumnIdentifier expectedIdentifierB = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
        ColumnIdentifier expectedIdentifierC = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
        ColumnIdentifier expectedIdentifierD = new ColumnIdentifier(this.relationName, this.columnNames.get(3));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC), expectedIdentifierA));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC), expectedIdentifierB));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC), expectedIdentifierD));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierA));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierB));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierC));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierB), expectedIdentifierC));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierB), expectedIdentifierD));

        verifyNoMoreInteractions(fdResultReceiver);
    }

}
