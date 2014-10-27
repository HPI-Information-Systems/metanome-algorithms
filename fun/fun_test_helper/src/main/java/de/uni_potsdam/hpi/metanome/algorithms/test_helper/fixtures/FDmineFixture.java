package de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;

public class FDmineFixture {

    protected ImmutableList<String> columnNames = ImmutableList.of("A", "B", "C", "D", "E", "F");
    protected int numberOfColumns = 6;
    protected int rowPosition;
    protected String relationName = "testTable";
    protected List<ImmutableList<String>> table = new LinkedList<>();
    protected FunctionalDependencyResultReceiver functionalDependencyResultReceiver = mock(FunctionalDependencyResultReceiver.class);
    protected UniqueColumnCombinationResultReceiver uniqueColumnCombinationResultReceiver = mock(UniqueColumnCombinationResultReceiver.class);
    protected InclusionDependencyResultReceiver inclusionDependencyResultReceiver = mock(InclusionDependencyResultReceiver.class);

    public FDmineFixture() throws CouldNotReceiveResultException {
        table.add(ImmutableList.of("1", "1", "1", "1", "1", "1"));
        table.add(ImmutableList.of("2", "1", "2", "2", "1", "2"));
        table.add(ImmutableList.of("3", "2", "3", "3", "2", "3"));
        table.add(ImmutableList.of("1", "3", "3", "1", "3", "3"));
        table.add(ImmutableList.of("2", "2", "1", "2", "2", "1"));
        table.add(ImmutableList.of("3", "3", "2", "3", "3", "2"));


        this.rowPosition = 0;

        // TODO remove debugging
//			doAnswer(new Answer() {
//				public Object answer(InvocationOnMock invocation) {
//					Object[] args = invocation.getArguments();
//					System.out.println(args[0]);
//					return null;
//				}
//			}).when(fdResultReceiver).receiveResult(isA(FunctionalDependency.class));

//			doAnswer(new Answer() {
//				public Object answer(InvocationOnMock invocation) {
//					Object[] args = invocation.getArguments();
//					System.out.println(args[0]);
//					return null;
//				}
//			}).when(inclusionDependencyResultReceiver).receiveResult(isA(InclusionDependency.class));

//			doAnswer(new Answer() {
//				public Object answer(InvocationOnMock invocation) {
//					Object[] args = invocation.getArguments();
//					System.out.println(args[0]);
//					return null;
//				}
//			}).when(uccResultReceiver).receiveResult(isA(UniqueColumnCombination.class));
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

    public FunctionalDependencyResultReceiver getFunctionalDependencyResultReceiver() {
        return this.functionalDependencyResultReceiver;
    }

    public UniqueColumnCombinationResultReceiver getUniqueColumnCombinationResultReceiver() {
        return this.uniqueColumnCombinationResultReceiver;
    }

    public InclusionDependencyResultReceiver getInclusionDependencyResultReceiver() {
        return this.inclusionDependencyResultReceiver;
    }


    public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException {
        ColumnIdentifier expectedIdentifierA = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
        ColumnIdentifier expectedIdentifierB = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
        ColumnIdentifier expectedIdentifierC = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
        ColumnIdentifier expectedIdentifierD = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
        ColumnIdentifier expectedIdentifierE = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
        ColumnIdentifier expectedIdentifierF = new ColumnIdentifier(this.relationName, this.columnNames.get(5));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA), expectedIdentifierD));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB), expectedIdentifierE));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC), expectedIdentifierF));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierA));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierE), expectedIdentifierB));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierF), expectedIdentifierC));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierE), expectedIdentifierC));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierE), expectedIdentifierF));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierE), expectedIdentifierA));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierE), expectedIdentifierD));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierE), expectedIdentifierC));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierE), expectedIdentifierF));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierB), expectedIdentifierC));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierB), expectedIdentifierF));


        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierE, expectedIdentifierF), expectedIdentifierA));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierE, expectedIdentifierF), expectedIdentifierD));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierF), expectedIdentifierB));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierF), expectedIdentifierE));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierC), expectedIdentifierA));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierC), expectedIdentifierD));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierF), expectedIdentifierA));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierF), expectedIdentifierD));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierD), expectedIdentifierC));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierD), expectedIdentifierF));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierD), expectedIdentifierB));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierD), expectedIdentifierE));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierC), expectedIdentifierB));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierC), expectedIdentifierE));

        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierF), expectedIdentifierB));
        verify(functionalDependencyResultReceiver, atLeastOnce()).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierF), expectedIdentifierE));

        //verifyNoMoreInteractions(fdResultReceiver);
    }
//		
//		public void verifyUniqueColumnCombinationResultReceiver() throws CouldNotReceiveResultException {
//			ColumnIdentifier expectedIdentifierPROF = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
//			ColumnIdentifier expectedIdentifierCSE = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
//			ColumnIdentifier expectedIdentifierDAY = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
//			ColumnIdentifier expectedIdentifierBEGIN = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
//			ColumnIdentifier expectedIdentifierEND = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
//			ColumnIdentifier expectedIdentifierROOM = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
//			//ColumnIdentifier expectedIdentifierCAP = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
//			ColumnIdentifier expectedIdentifierID = new ColumnIdentifier(this.relationName, this.columnNames.get(7));
//			
//			verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierPROF));
//			verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierCSE));
//			
//			verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierID, expectedIdentifierROOM));
//			
//			verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierDAY, expectedIdentifierBEGIN, expectedIdentifierROOM));
//			verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierDAY, expectedIdentifierEND, expectedIdentifierROOM));
//			
//			verifyNoMoreInteractions(uccResultReceiver);
//		}
//		
//		public void verifyInclusionDependencyResultReceiver() throws CouldNotReceiveResultException {
//			//ColumnIdentifier expectedIdentifierPROF = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
//			//ColumnIdentifier expectedIdentifierCSE = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
//			ColumnIdentifier expectedIdentifierDAY = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
//			ColumnIdentifier expectedIdentifierBEGIN = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
//			ColumnIdentifier expectedIdentifierEND = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
//			//ColumnIdentifier expectedIdentifierROOM = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
//			//ColumnIdentifier expectedIdentifierCAP = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
//			ColumnIdentifier expectedIdentifierID = new ColumnIdentifier(this.relationName, this.columnNames.get(7));
//			
//			verify(inclusionDependencyResultReceiver).receiveResult(new InclusionDependency(new ColumnCombination(expectedIdentifierBEGIN), new ColumnCombination(expectedIdentifierEND)));
//			verify(inclusionDependencyResultReceiver).receiveResult(new InclusionDependency(new ColumnCombination(expectedIdentifierDAY), new ColumnCombination(expectedIdentifierID)));
//
//			verifyNoMoreInteractions(inclusionDependencyResultReceiver);
//		}
//	}

}
