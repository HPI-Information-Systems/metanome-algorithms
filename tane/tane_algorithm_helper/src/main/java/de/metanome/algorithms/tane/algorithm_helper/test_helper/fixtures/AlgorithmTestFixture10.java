package de.metanome.algorithms.tane.algorithm_helper.test_helper.fixtures;

import com.google.common.collect.ImmutableList;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.FunctionalDependency;

import static org.mockito.Mockito.*;

public class AlgorithmTestFixture10 extends AbstractAlgorithmTestFixture {

    public AlgorithmTestFixture10() throws CouldNotReceiveResultException {
        columnNames = ImmutableList.of("PROF", "CSE", "Day", "BEGIN", "END", "ROOM", "CAP", "ID");
        numberOfColumns = 8;
        table.add(ImmutableList.of("NF", "AL", "Tuesday", "09:00", "11:00", "A2", "150", "1"));
        table.add(ImmutableList.of("DM", "NW", "Friday", "09:00", "11:00", "A2", "150", "2"));
        table.add(ImmutableList.of("ML", "OS", "Monday", "09:00", "12:00", "I10", "30", "3"));
        table.add(ImmutableList.of("NN", "PL", "Monday", "14:00", "17:00", "I10", "30", "4"));
        table.add(ImmutableList.of("AH", "DB", "Monday", "09:00", "12:00", "I11", "30", "3"));
        table.add(ImmutableList.of("RC", "SI", "Tuesday", "09:00", "12:00", "I10", "30", "5"));
        table.add(ImmutableList.of("KL", "OR", "Tuesday", "09:00", "12:00", "I12", "30", "5"));

        // TODO remove debugging
//		doAnswer(new Answer() {
//			public Object answer(InvocationOnMock invocation) {
//				Object[] args = invocation.getArguments();
//				System.out.println(args[0]);
//				System.out.println(args[1]);
//				return null;
//			}
//		}).when(functionalDependencyResultReceiver).receiveResult(isA(ColumnCombination.class), isA(ColumnIdentifier.class));
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
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(input.next())
                .thenReturn(this.table.get(0))
                .thenReturn(this.table.get(1))
                .thenReturn(this.table.get(2))
                .thenReturn(this.table.get(3))
                .thenReturn(this.table.get(4))
                .thenReturn(this.table.get(5))
                .thenReturn(this.table.get(6));

        return input;
    }

    public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException {
        ColumnIdentifier expPROF = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
        ColumnIdentifier expCSE = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
        ColumnIdentifier expDAY = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
        ColumnIdentifier expBEGIN = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
        ColumnIdentifier expEND = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
        ColumnIdentifier expROOM = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
        ColumnIdentifier expCAP = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
        ColumnIdentifier expID = new ColumnIdentifier(this.relationName, this.columnNames.get(7));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expPROF), expCSE));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expPROF), expDAY));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expPROF), expBEGIN));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expPROF), expEND));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expPROF), expROOM));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expPROF), expCAP));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expPROF), expID));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expCSE), expPROF));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expCSE), expDAY));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expCSE), expBEGIN));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expCSE), expEND));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expCSE), expROOM));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expCSE), expCAP));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expCSE), expID));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expID), expCAP));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expID), expDAY));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expID), expEND));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expID), expBEGIN));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expEND), expBEGIN));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expEND), expCAP));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expROOM), expCAP));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expBEGIN, expROOM), expEND));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expBEGIN, expCAP), expEND));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expID, expROOM), expPROF));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expID, expROOM), expCSE));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expDAY, expEND), expID));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expDAY, expBEGIN, expROOM), expCSE));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expDAY, expBEGIN, expROOM), expPROF));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expDAY, expBEGIN, expROOM), expID));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expDAY, expEND, expROOM), expCSE));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expDAY, expEND, expROOM), expPROF));

        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expDAY, expBEGIN, expCAP), expID));

        verifyNoMoreInteractions(fdResultReceiver);
    }

}
