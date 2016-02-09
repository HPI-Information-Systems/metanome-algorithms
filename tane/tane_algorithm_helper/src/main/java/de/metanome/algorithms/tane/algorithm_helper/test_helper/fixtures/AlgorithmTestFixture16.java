package de.metanome.algorithms.tane.algorithm_helper.test_helper.fixtures;

import com.google.common.collect.ImmutableList;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.FunctionalDependency;

import static org.mockito.Mockito.*;

public class AlgorithmTestFixture16 extends AbstractAlgorithmTestFixture {

    public AlgorithmTestFixture16() throws CouldNotReceiveResultException {
        this.columnNames = ImmutableList.of("A", "B", "C");
        this.numberOfColumns = 3;

        table.add(ImmutableList.of("1", "6", "11"));
        table.add(ImmutableList.of("2", "7", "12"));
        table.add(ImmutableList.of("3", "8", "13"));
        table.add(ImmutableList.of("4", "9", "14"));
        table.add(ImmutableList.of("5", "10", "15"));
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
                .thenReturn(table.get(0))
                .thenReturn(table.get(1))
                .thenReturn(table.get(2))
                .thenReturn(table.get(3))
                .thenReturn(table.get(4));


        return input;
    }

    public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException, ColumnNameMismatchException {
        ColumnIdentifier expectedIdentifierA = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
        ColumnIdentifier expectedIdentifierB = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
        ColumnIdentifier expectedIdentifierC = new ColumnIdentifier(this.relationName, this.columnNames.get(2));

        ColumnCombination coComA = new ColumnCombination(expectedIdentifierA);
        ColumnCombination coComB = new ColumnCombination(expectedIdentifierB);
        ColumnCombination coComC = new ColumnCombination(expectedIdentifierC);

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(coComA, expectedIdentifierB));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(coComA, expectedIdentifierC));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(coComB, expectedIdentifierA));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(coComB, expectedIdentifierC));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(coComC, expectedIdentifierA));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(coComC, expectedIdentifierB));


        verifyNoMoreInteractions(fdResultReceiver);
    }

}
