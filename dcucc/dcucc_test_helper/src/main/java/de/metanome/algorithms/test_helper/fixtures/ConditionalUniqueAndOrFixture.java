package de.metanome.algorithms.test_helper.fixtures;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.*;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Jens Ehrlich
 */
public class ConditionalUniqueAndOrFixture {

  protected ImmutableList<String>
      columnNames =
      ImmutableList.of("A", "B", "C");
  protected int numberOfColumns = 3;
  protected int rowPosition;
  protected String relationName = "testTable";
  protected RelationalInput input;
  protected List<ImmutableList<String>> table = new LinkedList<>();
  protected ConditionalUniqueColumnCombinationResultReceiver
      conditionalUniqueResultReceiver =
      mock(ConditionalUniqueColumnCombinationResultReceiver.class);

  public ConditionalUniqueAndOrFixture() throws CouldNotReceiveResultException {
    table.add(ImmutableList.of("1", "1", "3"));
    table.add(ImmutableList.of("1", "1", "1"));
    table.add(ImmutableList.of("2", "1", "2"));
    table.add(ImmutableList.of("3", "1", "1"));
    table.add(ImmutableList.of("4", "1", "2"));
    table.add(ImmutableList.of("5", "1", "1"));
    table.add(ImmutableList.of("5", "2", "1"));

    this.rowPosition = 0;

    doAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        System.out.println(args[0]);
        return null;
      }
    }).when(conditionalUniqueResultReceiver)
        .receiveResult(isA(ConditionalUniqueColumnCombination.class));
  }

  public static Map<ColumnCombinationBitset, PositionListIndex> getPlis(RelationalInput input)
      throws InputIterationException {
    Map<ColumnCombinationBitset, PositionListIndex> plis = new HashMap<>();
    PLIBuilder builder = new PLIBuilder(input);
    List<PositionListIndex> pliList = builder.getPLIList();
    int i = 0;
    for (PositionListIndex pli : pliList) {
      plis.put(new ColumnCombinationBitset(i++), pli);
    }
    return plis;
  }

  public RelationalInputGenerator getInputGenerator()
          throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
    RelationalInputGenerator inputGenerator = mock(RelationalInputGenerator.class);
    this.input = this.getRelationalInput();
    when(inputGenerator.generateNewCopy())
        .thenAnswer(new Answer<RelationalInput>() {
          public RelationalInput answer(InvocationOnMock invocation) throws Throwable {
            rowPosition = 0;
            return input;
          }
        });
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

  public ConditionalUniqueColumnCombinationResultReceiver getConditionalUniqueResultReceiver() {
    return this.conditionalUniqueResultReceiver;
  }

  public void verifiyConditionalUniqueColumnCombinationForAndOr()
      throws CouldNotReceiveResultException {
    ColumnIdentifier
        A = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        B = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        C = new ColumnIdentifier(this.relationName, this.columnNames.get(2));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(A),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(C, "2"),
                                                   new ColumnConditionAnd(
                                                       new ColumnConditionValue(B, "1"),
                                                       new ColumnConditionValue(C, "1")))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(A, B),
                                               new ColumnConditionOr(C, "1", "2")));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(A, C),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(B, "1"))));
    verifyNoMoreInteractions(conditionalUniqueResultReceiver);
  }
}
