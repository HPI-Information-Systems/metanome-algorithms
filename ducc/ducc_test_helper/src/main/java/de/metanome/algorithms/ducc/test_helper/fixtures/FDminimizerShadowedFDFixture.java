package de.metanome.algorithms.ducc.test_helper.fixtures;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
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
import de.metanome.algorithm_integration.results.UniqueColumnCombination;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FDminimizerShadowedFDFixture {

  protected ImmutableList<String> columnNames = ImmutableList.of("A", "B", "C", "D", "E", "F");
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

  public FDminimizerShadowedFDFixture() throws CouldNotReceiveResultException {
    table.add(ImmutableList.of("1", "0", "0", "0", "0", "3"));
    table.add(ImmutableList.of("0", "1", "0", "0", "0", "0"));
    table.add(ImmutableList.of("1", "1", "0", "0", "0", "0"));
    table.add(ImmutableList.of("0", "0", "1", "0", "1", "1"));
    table.add(ImmutableList.of("0", "0", "2", "0", "1", "2"));
    table.add(ImmutableList.of("0", "0", "0", "1", "2", "0"));
    table.add(ImmutableList.of("0", "0", "0", "2", "2", "0"));
    //table.add(ImmutableList.of("5", "5", "5", "5", "5", "0"));

    this.rowPosition = 0;

//		table.add(ImmutableList.of("NF", "AL", "Tuesday", "09:00", "11:00", "A2", "150", "1"));
//		table.add(ImmutableList.of("DM", "NW", "Friday", "09:00", "11:00", "A2", "150", "2"));
//		table.add(ImmutableList.of("ML", "OS", "Monday", "09:00", "12:00", "I10", "30", "3"));
//		table.add(ImmutableList.of("NN", "PL", "Monday", "14:00", "17:00", "I10", "30", "4"));
//		table.add(ImmutableList.of("AH", "DB", "Monday", "09:00", "12:00", "I11", "30", "3"));
//		table.add(ImmutableList.of("RC", "SI", "Tuesday", "09:00", "12:00", "I10", "30", "5"));
//		table.add(ImmutableList.of("KL", "OR", "Tuesday", "09:00", "12:00", "I12", "30", "5"));	

    // TODO remove debugging
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        System.out.println(args[0]);
        return null;
      }
    }).when(functionalDependencyResultReceiver).receiveResult(isA(FunctionalDependency.class));

//		doAnswer(new Answer() {
//			public Object answer(InvocationOnMock invocation) {
//				Object[] args = invocation.getArguments();
//				System.out.println(args[0]);
//				return null;
//			}
//		}).when(inclusionDependencyResultReceiver).receiveResult(isA(InclusionDependency.class));

    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        System.out.println(args[0]);
        return null;
      }
    }).when(uniqueColumnCombinationResultReceiver)
        .receiveResult(isA(UniqueColumnCombination.class));
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

  public List<ColumnCombinationBitset> getUCCList() {
    List<ColumnCombinationBitset> uccList = new LinkedList<>();
    uccList.add(new ColumnCombinationBitset(0));
    uccList.add(new ColumnCombinationBitset(1));
    uccList.add(new ColumnCombinationBitset(2, 3, 5));
    uccList.add(new ColumnCombinationBitset(2, 4, 5));
    uccList.add(new ColumnCombinationBitset(5, 7));
    return uccList;
  }

  public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException {
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
    ColumnIdentifier
        expectedIdentifierF =
        new ColumnIdentifier(this.relationName, this.columnNames.get(5));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierF), expectedIdentifierC));

    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierE, expectedIdentifierF),
                                 expectedIdentifierB));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierF),
                                 expectedIdentifierE));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierD),
                                 expectedIdentifierE));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierF),
                                 expectedIdentifierB));
    verify(functionalDependencyResultReceiver).receiveResult(
        new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierF),
                                 expectedIdentifierE));

    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierB, expectedIdentifierC, expectedIdentifierD),
        expectedIdentifierF));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierB, expectedIdentifierC, expectedIdentifierE),
        expectedIdentifierF));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierA, expectedIdentifierB, expectedIdentifierC),
        expectedIdentifierE));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierA, expectedIdentifierB, expectedIdentifierC),
        expectedIdentifierF));
    verify(functionalDependencyResultReceiver).receiveResult(new FunctionalDependency(
        new ColumnCombination(expectedIdentifierA, expectedIdentifierB, expectedIdentifierD),
        expectedIdentifierE));

    verifyNoMoreInteractions(functionalDependencyResultReceiver);
  }

//	public void verifyUniqueColumnCombinationResultReceiver() throws CouldNotReceiveResultException {
//		ColumnIdentifier expectedIdentifierA = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
//		ColumnIdentifier expectedIdentifierB = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
//		ColumnIdentifier expectedIdentifierC = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
//		ColumnIdentifier expectedIdentifierD = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
//		ColumnIdentifier expectedIdentifierE = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
//		
//		//verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifierPROF));
//
//		verifyNoMoreInteractions(uccResultReceiver);
//	}
}
