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
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;

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

public class AlgorithmTestFixture {

  protected ImmutableList<String>
      columnNames =
      ImmutableList.of("PROF", "CSE", "DAY", "BEGIN", "END", "ROOM", "CAP", "ID");
  protected int numberOfColumns = 8;
  protected int rowPosition;
  protected String relationName = "testTable";
  protected RelationalInput input;
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
  protected ConditionalUniqueColumnCombinationResultReceiver
      conditionalUniqueResultReceiver =
      mock(ConditionalUniqueColumnCombinationResultReceiver.class);

  public AlgorithmTestFixture() throws CouldNotReceiveResultException, ColumnNameMismatchException {
    table.add(ImmutableList.of("NF", "AL", "Tuesday", "09:00", "09:00", "A2", "150", "Monday"));
    table.add(ImmutableList.of("DM", "NW", "Friday", "09:00", "09:00", "A2", "150", "Tuesday"));
    table.add(ImmutableList.of("ML", "OS", "Monday", "09:00", "14:00", "I10", "30", "Wednesday"));
    table.add(ImmutableList.of("NN", "PL", "Monday", "14:00", "17:00", "I10", "30", "Thursday"));
    table.add(ImmutableList.of("AH", "DB", "Monday", "09:00", "14:00", "I11", "30", "Wednesday"));
    table.add(ImmutableList.of("RC", "SI", "Tuesday", "09:00", "14:00", "I10", "30", "Friday"));
    table.add(ImmutableList.of("KL", "OR", "Tuesday", "09:00", "14:00", "I12", "30", "Friday"));

    this.rowPosition = 0;

    // TODO remove debugging
//		doAnswer(new Answer() {
//			public Object answer(InvocationOnMock invocation) {
//				Object[] args = invocation.getArguments();
//				System.out.println(args[0]);
//				return null;
//			}
//		}).when(fdResultReceiver).receiveResult(isA(FunctionalDependency.class));
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

    doAnswer(new Answer<Object>() {
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

  public FunctionalDependencyResultReceiver getFunctionalDependencyResultReceiver() {
    return this.functionalDependencyResultReceiver;
  }

  public UniqueColumnCombinationResultReceiver getUniqueColumnCombinationResultReceiver() {
    return this.uniqueColumnCombinationResultReceiver;
  }

  public InclusionDependencyResultReceiver getInclusionDependencyResultReceiver() {
    return this.inclusionDependencyResultReceiver;
  }

  public ConditionalUniqueColumnCombinationResultReceiver getConditionalUniqueResultReceiver() {
    return this.conditionalUniqueResultReceiver;
  }

  public List<ColumnCombinationBitset> getUCCList() {
    List<ColumnCombinationBitset> uccList = new LinkedList<>();
    uccList.add(new ColumnCombinationBitset(0));
    uccList.add(new ColumnCombinationBitset(1));
    uccList.add(new ColumnCombinationBitset(2, 3, 5));
    uccList.add(new ColumnCombinationBitset(2, 4, 5));
    uccList.add(new ColumnCombinationBitset(5, 7));
    return uccList;
  }

  public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException, ColumnNameMismatchException {
    ColumnIdentifier
        expectedIdentifierPROF =
        new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        expectedIdentifierCSE =
        new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        expectedIdentifierDAY =
        new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        expectedIdentifierBEGIN =
        new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        expectedIdentifierEND =
        new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    ColumnIdentifier
        expectedIdentifierROOM =
        new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    ColumnIdentifier
        expectedIdentifierCAP =
        new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    ColumnIdentifier
        expectedIdentifierID =
        new ColumnIdentifier(this.relationName, this.columnNames.get(7));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierPROF),
                                 expectedIdentifierCSE));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierPROF),
                                 expectedIdentifierDAY));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierPROF),
                                 expectedIdentifierBEGIN));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierPROF),
                                 expectedIdentifierEND));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierPROF),
                                 expectedIdentifierROOM));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierPROF),
                                 expectedIdentifierCAP));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierPROF),
                                 expectedIdentifierID));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierCSE),
                                 expectedIdentifierPROF));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierCSE),
                                 expectedIdentifierDAY));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierCSE),
                                 expectedIdentifierBEGIN));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierCSE),
                                 expectedIdentifierEND));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierCSE),
                                 expectedIdentifierROOM));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierCSE),
                                 expectedIdentifierCAP));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierCSE),
                                 expectedIdentifierID));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierID),
                                 expectedIdentifierCAP));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierID),
                                 expectedIdentifierDAY));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierID),
                                 expectedIdentifierEND));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierID),
                                 expectedIdentifierBEGIN));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierEND),
                                 expectedIdentifierBEGIN));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierEND),
                                 expectedIdentifierCAP));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierROOM),
                                 expectedIdentifierCAP));

    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierBEGIN, expectedIdentifierROOM),
        expectedIdentifierEND));

    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierBEGIN, expectedIdentifierCAP),
        expectedIdentifierEND));

    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierID, expectedIdentifierROOM),
        expectedIdentifierPROF));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierID, expectedIdentifierROOM),
        expectedIdentifierCSE));

    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierDAY, expectedIdentifierEND), expectedIdentifierID));

    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierDAY, expectedIdentifierBEGIN,
                              expectedIdentifierROOM), expectedIdentifierCSE));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierDAY, expectedIdentifierBEGIN,
                              expectedIdentifierROOM), expectedIdentifierPROF));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierDAY, expectedIdentifierBEGIN,
                              expectedIdentifierROOM), expectedIdentifierID));

    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierDAY, expectedIdentifierEND, expectedIdentifierROOM),
        expectedIdentifierCSE));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierDAY, expectedIdentifierEND, expectedIdentifierROOM),
        expectedIdentifierPROF));

    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierDAY, expectedIdentifierBEGIN,
                              expectedIdentifierCAP), expectedIdentifierID));

    //verifyNoMoreInteractions(fdResultReceiver);
  }

  public void verifyUniqueColumnCombinationResultReceiver() throws CouldNotReceiveResultException, ColumnNameMismatchException {
    ColumnIdentifier
        expectedIdentifierPROF =
        new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        expectedIdentifierCSE =
        new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        expectedIdentifierDAY =
        new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        expectedIdentifierBEGIN =
        new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        expectedIdentifierEND =
        new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    ColumnIdentifier
        expectedIdentifierROOM =
        new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    //ColumnIdentifier expectedIdentifierCAP = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    ColumnIdentifier
        expectedIdentifierID =
        new ColumnIdentifier(this.relationName, this.columnNames.get(7));

    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(expectedIdentifierPROF));
    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(expectedIdentifierCSE));

    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(expectedIdentifierID, expectedIdentifierROOM));

    verify(uniqueColumnCombinationResultReceiver).receiveResult(
        new UniqueColumnCombination(expectedIdentifierDAY, expectedIdentifierBEGIN,
                                    expectedIdentifierROOM));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(
        new UniqueColumnCombination(expectedIdentifierDAY, expectedIdentifierEND,
                                    expectedIdentifierROOM));

    verifyNoMoreInteractions(uniqueColumnCombinationResultReceiver);
  }

  public void verifyConditionalUniqueColumnCombinationFor4() throws CouldNotReceiveResultException, ColumnNameMismatchException {
    ColumnIdentifier
        prof = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        cse = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        day = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        begin = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        end = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    ColumnIdentifier
        room = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    ColumnIdentifier
        cap = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    ColumnIdentifier
        id = new ColumnIdentifier(this.relationName, this.columnNames.get(7));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(begin, "09:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(end, "14:00"))));

    ColumnConditionAnd innerCondition2 = new ColumnConditionAnd();
    innerCondition2.setNegated(true);
    innerCondition2.add(new ColumnConditionValue(day, "Monday"));
    innerCondition2.add(new ColumnConditionValue(room, "I10"));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionAnd(innerCondition2)));

    verifyNoMoreInteractions(conditionalUniqueResultReceiver);

  }

  public void verifyConditionalUniqueColumnCombinationFor3() throws CouldNotReceiveResultException, ColumnNameMismatchException {
    ColumnIdentifier
        prof = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        cse = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        day = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        begin = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        end = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    ColumnIdentifier
        room = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    ColumnIdentifier
        cap = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    ColumnIdentifier
        id = new ColumnIdentifier(this.relationName, this.columnNames.get(7));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(day, "Tuesday"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(id),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, end),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(end, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(day, "Monday"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(begin, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(begin, "09:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(end, "14:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(begin, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(day, "Monday"))));

    verifyNoMoreInteractions(conditionalUniqueResultReceiver);
  }

  public void verifyConditionalUniqueColumnCombinationFor4OrConditions()
      throws CouldNotReceiveResultException, ColumnNameMismatchException {
    ColumnIdentifier
        prof = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        cse = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        day = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        begin = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        end = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    ColumnIdentifier
        room = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    ColumnIdentifier
        cap = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    ColumnIdentifier
        id = new ColumnIdentifier(this.relationName, this.columnNames.get(7));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(begin, "09:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(id),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2"),
                                                   new ColumnConditionValue(room,
                                                                            "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, end),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2"),
                                                   new ColumnConditionValue(room,
                                                                            "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(end, "14:00"),
                                                   new ColumnConditionValue(end, "09:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(id, "Friday"),
                                                   new ColumnConditionValue(id, "Wednesday"))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(begin, cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2")
                                                   , new ColumnConditionValue(room, "I10"))));

    verifyNoMoreInteractions(conditionalUniqueResultReceiver);
  }

  public void verifyConditionalUniqueColumnCombinationFor4AndOrConditions()
      throws CouldNotReceiveResultException, ColumnNameMismatchException {
    ColumnIdentifier
        prof = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        cse = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        day = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        begin = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        end = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    ColumnIdentifier
        room = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    ColumnIdentifier
        cap = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    ColumnIdentifier
        id = new ColumnIdentifier(this.relationName, this.columnNames.get(7));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(id),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2"),
                                                   new ColumnConditionValue(room,
                                                                            "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, end),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2"),
                                                   new ColumnConditionValue(room,
                                                                            "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(begin, "09:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(end, "14:00"),
                                                   new ColumnConditionValue(end, "09:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(id, "Friday"),
                                                   new ColumnConditionValue(id, "Wednesday"))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(begin, cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2")
                                                   , new ColumnConditionValue(room, "I10"))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2")
                                                   , new ColumnConditionAnd(
                                                   new ColumnConditionValue(room, "I10"),
                                                   new ColumnConditionValue(begin, "09:00")))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(end, "09:00")
                                                   , new ColumnConditionAnd(
                                                   new ColumnConditionValue(room, "I10"),
                                                   new ColumnConditionValue(end, "14:00")))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2")
                                                   , new ColumnConditionAnd(
                                                   new ColumnConditionValue(room, "I10"),
                                                   new ColumnConditionValue(end, "14:00")))));

    verifyNoMoreInteractions(conditionalUniqueResultReceiver);
  }

  public void verifyConditionalUniqueColumnCombinationFor3AndOrConditions()
      throws CouldNotReceiveResultException, ColumnNameMismatchException {
    ColumnIdentifier
        prof = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        cse = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        day = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        begin = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        end = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    ColumnIdentifier
        room = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    ColumnIdentifier
        cap = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    ColumnIdentifier
        id = new ColumnIdentifier(this.relationName, this.columnNames.get(7));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(id),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2"),
                                                   new ColumnConditionValue(room,
                                                                            "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, end),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2"),
                                                   new ColumnConditionValue(room,
                                                                            "I10"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(begin, "09:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(end, "14:00"),
                                                   new ColumnConditionValue(end, "09:00"))));
    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(day, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(id, "Friday"),
                                                   new ColumnConditionValue(id, "Wednesday"))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(begin, cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2")
                                                   , new ColumnConditionValue(room, "I10"))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2")
                                                   , new ColumnConditionAnd(
                                                   new ColumnConditionValue(room, "I10"),
                                                   new ColumnConditionValue(begin, "09:00")))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(end, "09:00")
                                                   , new ColumnConditionAnd(
                                                   new ColumnConditionValue(room, "I10"),
                                                   new ColumnConditionValue(end, "14:00")))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(cap, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "A2")
                                                   , new ColumnConditionAnd(
                                                   new ColumnConditionValue(room, "I10"),
                                                   new ColumnConditionValue(end, "14:00")))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(day, "Tuesday"))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(begin, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(day, "Monday"))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(end, room),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(day, "Monday"))));

    verify(conditionalUniqueResultReceiver).receiveResult(
        new ConditionalUniqueColumnCombination(new ColumnCombination(begin, day),
                                               new ColumnConditionOr(
                                                   new ColumnConditionValue(room, "I10"))));

    verifyNoMoreInteractions(conditionalUniqueResultReceiver);
  }
}
