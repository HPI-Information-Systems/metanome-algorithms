package de.metanome.algorithms.ducc;

import com.google.common.collect.ImmutableList;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

// TODO: inherit logic from AlgorithmTestFixture
public class DuccTestFixtureWithHoles {

    protected ImmutableList<String> columnNames = ImmutableList.of("Day", "BEGIN", "END", "ROOM", "CAP");
    protected int numberOfColumns = this.columnNames.size();
    protected String relationName = "testTable";
    protected List<ImmutableList<String>> table = new LinkedList<>();
    protected UniqueColumnCombinationResultReceiver uniqueColumnCombinationResultReceiver = mock(UniqueColumnCombinationResultReceiver.class);
    protected int rowPosition;

    public DuccTestFixtureWithHoles() throws CouldNotReceiveResultException {
        table.add(ImmutableList.of("1", "1", "1", "1", "1"));
        table.add(ImmutableList.of("1", "2", "2", "1", "1"));
        table.add(ImmutableList.of("1", "1", "1", "1", "2"));
        table.add(ImmutableList.of("2", "2", "1", "2", "1"));
        table.add(ImmutableList.of("3", "2", "2", "2", "1"));

        this.rowPosition = 0;

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println(args[0]);
                return null;
            }
        }).when(uniqueColumnCombinationResultReceiver).receiveResult(isA(UniqueColumnCombination.class));
    }

    public RelationalInputGenerator getInputGenerator() throws InputGenerationException, InputIterationException {
        RelationalInputGenerator inputGenerator = mock(RelationalInputGenerator.class);
        RelationalInput input = this.getRelationalInput();
        when(inputGenerator.generateNewCopy())
                .thenReturn(input);
        return inputGenerator;
    }

    protected RelationalInput getRelationalInput() throws InputIterationException {
        RelationalInput input = mock(RelationalInput.class);

        when(input.columnNames())
                .thenReturn(this.columnNames);
        when(input.numberOfColumns())
                .thenReturn(this.numberOfColumns);
        when(input.relationName())
                .thenReturn(this.relationName);

        when(input.hasNext()).thenAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return rowPosition < table.size();
            }
        });

        when(input.next()).thenAnswer(new Answer<ImmutableList<String>>() {
            public ImmutableList<String> answer(InvocationOnMock invocation) throws Throwable {
                rowPosition += 1;
                return table.get(rowPosition - 1);
            }
        });

        return input;
    }

    //TODO: find path to force holes
    public UccGraphTraverser createSpyForGraphTraverser(UccGraphTraverser original) {
        return original;
//		UccGraphTraverser spy = spy(original);
//		
//		stub(spy.getRandomPositionOfList(5))
//			.toReturn(0)
//			.toReturn(0)
//			.toReturn(1)
//			.toReturn(1)
//			.toReturn(0)
//			.toReturn(0)
//			.toReturn(0);
//		
//		return spy;
    }

    public UniqueColumnCombinationResultReceiver getUniqueColumnCombinationResultReceiver() {
        return this.uniqueColumnCombinationResultReceiver;
    }

    public void verifyUniqueColumnCombinationResultReceiver() throws CouldNotReceiveResultException {
        ColumnIdentifier expectedIdentifierDAY = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
        ColumnIdentifier expectedIdentifierBEGIN = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
        ColumnIdentifier expectedIdentifierEND = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
        ColumnIdentifier expectedIdentifierROOM = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
        ColumnIdentifier expectedIdentifierCAP = new ColumnIdentifier(this.relationName, this.columnNames.get(4));

        verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierDAY, expectedIdentifierBEGIN, expectedIdentifierCAP));
        verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierEND, expectedIdentifierROOM, expectedIdentifierCAP));
        verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierDAY, expectedIdentifierEND, expectedIdentifierCAP));

        verifyNoMoreInteractions(uniqueColumnCombinationResultReceiver);
    }
}
