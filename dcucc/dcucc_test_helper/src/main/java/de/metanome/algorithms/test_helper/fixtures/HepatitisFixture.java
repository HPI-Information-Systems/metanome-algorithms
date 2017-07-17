package de.metanome.algorithms.test_helper.fixtures;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;
import de.metanome.backend.input.file.DefaultFileInputGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Jens Ehrlich
 */
public class HepatitisFixture {

  protected ImmutableList<String>
      columnNames =
      ImmutableList.of("column1", "column2", "column3", "column4", "column5", "column6", "column7",
                       "column8", "column9", "column10", "column11", "column12", "column13",
                       "column14", "column15", "column16", "column17", "column18", "column19",
                       "column20");
  protected int numberOfColumns = 20;
  protected int rowPosition;
  protected String relationName = "hepatitis.csv";
  protected List<ImmutableList<String>> table = new LinkedList<>();
  protected FunctionalDependencyResultReceiver
      fdResultReceiver =
      mock(FunctionalDependencyResultReceiver.class);
  protected UniqueColumnCombinationResultReceiver
      uniqueColumnCombinationResultReceiver =
      mock(UniqueColumnCombinationResultReceiver.class);
  protected InclusionDependencyResultReceiver
      inclusionDependencyResultReceiver =
      mock(InclusionDependencyResultReceiver.class);
  protected ConditionalUniqueColumnCombinationResultReceiver
      cuccResultReceiver =
      mock(ConditionalUniqueColumnCombinationResultReceiver.class);

  public HepatitisFixture() throws CouldNotReceiveResultException {

/*
    doAnswer(new Answer() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        System.out.println(args[0]);
        return null;
      }
    }).when(uniqueColumnCombinationResultReceiver).receiveResult(isA(UniqueColumnCombination.class));
*/

  }

  public RelationalInputGenerator getInputGenerator()
      throws InputGenerationException, InputIterationException, UnsupportedEncodingException,
             FileNotFoundException {
    String
        pathToInputFile =
        URLDecoder.decode(
            Thread.currentThread().getContextClassLoader().getResource(relationName).getPath(),
            "utf-8");
    RelationalInputGenerator
        inputGenerator =
        new DefaultFileInputGenerator(new File(pathToInputFile));
    return inputGenerator;
  }

  public ConditionalUniqueColumnCombinationResultReceiver getCUCCResultReceiver() {
    return cuccResultReceiver;
  }


  public UniqueColumnCombinationResultReceiver getUCCResultReceiver() {
    return uniqueColumnCombinationResultReceiver;
  }

  public void verifyConditionalUniqueColumnCombination() throws CouldNotReceiveResultException {
    ColumnIdentifier
        c1 = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    ColumnIdentifier
        c2 = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    ColumnIdentifier
        c3 = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    ColumnIdentifier
        c4 = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    ColumnIdentifier
        c5 = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    ColumnIdentifier
        c6 = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    ColumnIdentifier
        c7 = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    ColumnIdentifier
        c8 = new ColumnIdentifier(this.relationName, this.columnNames.get(7));
    ColumnIdentifier
        c9 = new ColumnIdentifier(this.relationName, this.columnNames.get(8));
    ColumnIdentifier
        c10 = new ColumnIdentifier(this.relationName, this.columnNames.get(9));
    ColumnIdentifier
        c11 = new ColumnIdentifier(this.relationName, this.columnNames.get(10));
    ColumnIdentifier
        c12 = new ColumnIdentifier(this.relationName, this.columnNames.get(11));
    ColumnIdentifier
        c13 = new ColumnIdentifier(this.relationName, this.columnNames.get(12));
    ColumnIdentifier
        c14 = new ColumnIdentifier(this.relationName, this.columnNames.get(13));
    ColumnIdentifier
        c15 = new ColumnIdentifier(this.relationName, this.columnNames.get(14));
    ColumnIdentifier
        c16 = new ColumnIdentifier(this.relationName, this.columnNames.get(15));
    ColumnIdentifier
        c17 = new ColumnIdentifier(this.relationName, this.columnNames.get(16));
    ColumnIdentifier
        c18 = new ColumnIdentifier(this.relationName, this.columnNames.get(17));
    ColumnIdentifier
        c19 = new ColumnIdentifier(this.relationName, this.columnNames.get(18));
    ColumnIdentifier
        c20 = new ColumnIdentifier(this.relationName, this.columnNames.get(19));

    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c2));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c4));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c6));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c7));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c8));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c9));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c10));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c11));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c12));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c13));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c14));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c15));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c16));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c17));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c18));
    verify(uniqueColumnCombinationResultReceiver).receiveResult(new UniqueColumnCombination(c19));

    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(c1, c5));
    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(c1, c3));
    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(c20, c5));
    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(c1, c20));
    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(c20, c3));
    verify(uniqueColumnCombinationResultReceiver)
        .receiveResult(new UniqueColumnCombination(c5, c3));

    verifyNoMoreInteractions(uniqueColumnCombinationResultReceiver);
  }
}
