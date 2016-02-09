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
import de.metanome.algorithm_integration.results.FunctionalDependency;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FunFastCountFixture {

  protected ImmutableList<String> columnNames = ImmutableList.of("A", "B", "C", "D", "E");
  protected int numberOfColumns = 5;
  protected String relationName = "testTable";
  protected List<ImmutableList<String>> table = new LinkedList<>();
  protected FunctionalDependencyResultReceiver
      functionalDependencyResultReceiver =
      mock(FunctionalDependencyResultReceiver.class);

  public FunFastCountFixture() throws CouldNotReceiveResultException {
    table.add(ImmutableList.of("1", "1", "2", "1", "1"));
    table.add(ImmutableList.of("1", "2", "1", "5", "2"));
    table.add(ImmutableList.of("2", "1", "1", "2", "3"));
    table.add(ImmutableList.of("3", "1", "1", "2", "4"));
    table.add(ImmutableList.of("4", "1", "1", "2", "5"));
    table.add(ImmutableList.of("5", "1", "2", "1", "6"));
    table.add(ImmutableList.of("1", "1", "1", "4", "7"));
    table.add(ImmutableList.of("1", "1", "1", "4", "8"));
    table.add(ImmutableList.of("1", "1", "1", "7", "9"));
    table.add(ImmutableList.of("1", "2", "1", "5", "10"));

    // TODO remove debugging
//		doAnswer(new Answer() {
//			public Object answer(InvocationOnMock invocation) {
//				Object[] args = invocation.getArguments();
//				System.out.println(args[0]);
//				return null;
//			}
//		}).when(fdResultReceiver).receiveResult(isA(FunctionalDependency.class));
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

    when(input.hasNext())
        .thenReturn(true)
        .thenReturn(true)
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
        .thenReturn(this.table.get(6))
        .thenReturn(this.table.get(7))
        .thenReturn(this.table.get(8))
        .thenReturn(this.table.get(9));

    return input;
  }

  public FunctionalDependencyResultReceiver getFunctionalDependencyResultReceiver() {
    return this.functionalDependencyResultReceiver;
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
        new FunctionalDependency(new ColumnCombination(expectedIdentifierE), expectedIdentifierA));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierE), expectedIdentifierB));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierE), expectedIdentifierC));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierE), expectedIdentifierD));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierB));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierC));

    verifyNoMoreInteractions(functionalDependencyResultReceiver);

  }
}