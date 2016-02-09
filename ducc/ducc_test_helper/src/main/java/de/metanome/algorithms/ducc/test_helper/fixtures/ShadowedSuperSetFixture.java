package de.metanome.algorithms.ducc.test_helper.fixtures;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ShadowedSuperSetFixture {

  // FIXME E must be in a mUCC but we do not want to have more FDs
  protected ImmutableList<String> columnNames = ImmutableList.of("A", "B", "C", "D", "E");
  protected int numberOfColumns = 5;
  protected int rowPosition;
  protected String relationName = "testTable";
  protected List<ImmutableList<String>> table = new LinkedList<>();
  protected FunctionalDependencyResultReceiver
      functionalDependencyResultReceiver =
      mock(FunctionalDependencyResultReceiver.class);
  protected UniqueColumnCombinationResultReceiver
      uniqueColumnCombinationResultReceiver =
      mock(UniqueColumnCombinationResultReceiver.class);
  protected InclusionDependencyResultReceiver
      inclusionDependencyResultReceiver =
      mock(InclusionDependencyResultReceiver.class);

  public ShadowedSuperSetFixture() throws CouldNotReceiveResultException {
    table.add(ImmutableList.of("1", "1", "1", "1", "1"));
    table.add(ImmutableList.of("2", "1", "1", "1", "1"));
    table.add(ImmutableList.of("3", "2", "1", "1", "1"));
    table.add(ImmutableList.of("3", "3", "1", "1", "1"));
    table.add(ImmutableList.of("3", "1", "2", "1", "1"));
    table.add(ImmutableList.of("3", "1", "3", "1", "1"));
    table.add(ImmutableList.of("3", "4", "1", "1", "2"));
    table.add(ImmutableList.of("3", "1", "4", "4", "3"));
    table.add(ImmutableList.of("4", "1", "1", "5", "1"));
    table.add(ImmutableList.of("4", "5", "1", "6", "1"));
    table.add(ImmutableList.of("4", "5", "5", "7", "1"));
    this.rowPosition = 0;

    // TODO remove debugging
//		doAnswer(new Answer() {
//			public Object answer(InvocationOnMock invocation) {
//				Object[] args = invocation.getArguments();
//				System.out.println(args[0]);
//				return null;
//			}
//		}).when(functionalDependencyResultReceiver).receiveResult(isA(FunctionalDependency.class));
//		
//		doAnswer(new Answer() {
//			public Object answer(InvocationOnMock invocation) {
//				Object[] args = invocation.getArguments();
//				System.out.println(args[0]);
//				return null;
//			}
//		}).when(inclusionDependencyResultReceiver).receiveResult(isA(InclusionDependency.class));

//		doAnswer(new Answer() {
//			public Object answer(InvocationOnMock invocation) {
//				Object[] args = invocation.getArguments();
//				System.out.println(args[0]);
//				return null;
//			}
//		}).when(uccResultReceiver).receiveResult(isA(UniqueColumnCombination.class));
  }

  public RelationalInputGenerator getInputGenerator()
      throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
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

  public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException, ColumnNameMismatchException {
    ColumnIdentifier
        expectedIdentifierA =
        new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        expectedIdentifierB =
        new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        expectedIdentifierC =
        new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        expectedIdentifierD =
        new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        expectedIdentifierE =
        new ColumnIdentifier(this.relationName, this.columnNames.get(4));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierC),
                                 expectedIdentifierE));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierD),
                                 expectedIdentifierE));
    //verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierB, expectedIdentifierE), expectedIdentifierD));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierA, expectedIdentifierB, expectedIdentifierC),
        expectedIdentifierD));

    verifyNoMoreInteractions(functionalDependencyResultReceiver);
  }

}

