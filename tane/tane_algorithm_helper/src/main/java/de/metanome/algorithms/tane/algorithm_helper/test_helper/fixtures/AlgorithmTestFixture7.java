package de.metanome.algorithms.tane.algorithm_helper.test_helper.fixtures;

import com.google.common.collect.ImmutableList;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.FunctionalDependency;

import static org.mockito.Mockito.*;

public class AlgorithmTestFixture7 extends AbstractAlgorithmTestFixture {

    public AlgorithmTestFixture7() throws CouldNotReceiveResultException {
        columnNames = ImmutableList.of("A", "B", "C");
        numberOfColumns = 3;
        table.add(ImmutableList.of("1", "2", "5"));
        table.add(ImmutableList.of("2", "2", "4"));
        table.add(ImmutableList.of("3", "3", "4"));
        table.add(ImmutableList.of("4", "3", "4"));

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
                .thenReturn(false);

        when(input.next())
                .thenReturn(this.table.get(0))
                .thenReturn(this.table.get(1))
                .thenReturn(this.table.get(2))
                .thenReturn(this.table.get(3));

        return input;
    }

    public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException {
        ColumnIdentifier expectedIdentifierA = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
        ColumnIdentifier expectedIdentifierB = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
        ColumnIdentifier expectedIdentifierC = new ColumnIdentifier(this.relationName, this.columnNames.get(2));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA), expectedIdentifierB));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA), expectedIdentifierC));

        verifyNoMoreInteractions(fdResultReceiver);
    }

}
