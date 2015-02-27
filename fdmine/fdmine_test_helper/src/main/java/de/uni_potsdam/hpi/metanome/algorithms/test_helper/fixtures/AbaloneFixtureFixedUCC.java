package de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures;

/**
 * Created by Jens on 10.02.14.
 */

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;
import de.metanome.backend.input.csv.DefaultFileInputGenerator;

public class AbaloneFixtureFixedUCC {

    protected ImmutableList<String> columnNames = ImmutableList.of("column1", "column2", "column3", "column4", "column5", "column6", "column7", "column8", "column9");
    protected int numberOfColumns = 9;
    protected int rowPosition;
    protected String relationName = "abalone.csv";
    protected ColumnIdentifier expectedIdentifier1 = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
    protected ColumnIdentifier expectedIdentifier2 = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
    protected ColumnIdentifier expectedIdentifier3 = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
    protected ColumnIdentifier expectedIdentifier4 = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
    protected ColumnIdentifier expectedIdentifier5 = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
    protected ColumnIdentifier expectedIdentifier6 = new ColumnIdentifier(this.relationName, this.columnNames.get(5));
    protected ColumnIdentifier expectedIdentifier7 = new ColumnIdentifier(this.relationName, this.columnNames.get(6));
    protected ColumnIdentifier expectedIdentifier8 = new ColumnIdentifier(this.relationName, this.columnNames.get(7));
    protected ColumnIdentifier expectedIdentifier9 = new ColumnIdentifier(this.relationName, this.columnNames.get(8));
    protected List<ImmutableList<String>> table = new LinkedList<>();
    protected FunctionalDependencyResultReceiver fdResultReceiver = mock(FunctionalDependencyResultReceiver.class);
    protected UniqueColumnCombinationResultReceiver uccResultReceiver = mock(UniqueColumnCombinationResultReceiver.class);
    protected InclusionDependencyResultReceiver inclusionDependencyResultReceiver = mock(InclusionDependencyResultReceiver.class);

    public AbaloneFixtureFixedUCC() throws CouldNotReceiveResultException {
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println(args[0]);
                return null;
            }
        }).when(fdResultReceiver).receiveResult(isA(FunctionalDependency.class));

//		doAnswer(new Answer() {
//			public Object answer(InvocationOnMock invocation) {
//				Object[] args = invocation.getArguments();
//				System.out.println(args[0]);
//				return null;
//			}
//		}).when(inclusionDependencyResultReceiver).receiveResult(isA(InclusionDependency.class));

//        doAnswer(new Answer() {
//            public Object answer(InvocationOnMock invocation) {
//                Object[] args = invocation.getArguments();
//                System.out.println(args[0]);
//                return null;
//            }
//        }).when(uccResultReceiver).receiveResult(isA(UniqueColumnCombination.class));
    }

    public RelationalInputGenerator getInputGenerator() throws UnsupportedEncodingException, FileNotFoundException, AlgorithmConfigurationException {
    	String pathToInputFile = URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource(relationName).getPath(), "utf-8");
    	RelationalInputGenerator inputGenerator = new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
        		pathToInputFile, true, ',', '"', '\\', false, true, 0, false, true, ""));
        return inputGenerator;
    }

    public List<ColumnCombinationBitset> getUCCList() {
        List<ColumnCombinationBitset> uccList = new LinkedList<>();

        uccList.add(new ColumnCombinationBitset(1, 4, 6, 8));
        uccList.add(new ColumnCombinationBitset(1, 3, 4, 6));
        uccList.add(new ColumnCombinationBitset(1, 2, 4, 7));
        uccList.add(new ColumnCombinationBitset(2, 3, 4, 7, 8));
        uccList.add(new ColumnCombinationBitset(2, 4, 6, 8));
        uccList.add(new ColumnCombinationBitset(2, 3, 4, 5));
        uccList.add(new ColumnCombinationBitset(4, 5, 7));
        uccList.add(new ColumnCombinationBitset(1, 3, 4, 5));
        uccList.add(new ColumnCombinationBitset(3, 4, 5, 6));
        uccList.add(new ColumnCombinationBitset(3, 5, 6, 7));
        uccList.add(new ColumnCombinationBitset(3, 5, 6, 8));
        uccList.add(new ColumnCombinationBitset(0, 1, 3, 5, 6));
        uccList.add(new ColumnCombinationBitset(0, 2, 3, 5, 6));
        uccList.add(new ColumnCombinationBitset(1, 2, 3, 5, 7, 8));
        uccList.add(new ColumnCombinationBitset(0, 1, 2, 3, 6, 7));
        uccList.add(new ColumnCombinationBitset(1, 5, 6, 8));
        uccList.add(new ColumnCombinationBitset(1, 2, 3, 6, 8));
        uccList.add(new ColumnCombinationBitset(0, 1, 2, 3, 5, 8));
        uccList.add(new ColumnCombinationBitset(0, 1, 2, 3, 5, 7));
        uccList.add(new ColumnCombinationBitset(0, 1, 4, 5));
        uccList.add(new ColumnCombinationBitset(1, 4, 5, 6));
        uccList.add(new ColumnCombinationBitset(2, 4, 5, 8));
        uccList.add(new ColumnCombinationBitset(4, 5, 6, 8));
        uccList.add(new ColumnCombinationBitset(4, 6, 7));
        uccList.add(new ColumnCombinationBitset(1, 2, 6, 7, 8));
        uccList.add(new ColumnCombinationBitset(0, 1, 6, 7, 8));
        uccList.add(new ColumnCombinationBitset(1, 2, 3, 4, 8));
        uccList.add(new ColumnCombinationBitset(0, 4, 6, 8));
        uccList.add(new ColumnCombinationBitset(1, 2, 4, 5));


        return uccList;
    }

    public FunctionalDependencyResultReceiver getFdResultReceiver() {
        return this.fdResultReceiver;
    }

    public UniqueColumnCombinationResultReceiver getUccResultReceiver() {
        return this.uccResultReceiver;
    }

    public InclusionDependencyResultReceiver getInclusionDependencyResultReceiver() {
        return this.inclusionDependencyResultReceiver;
    }

    public void verifyFdResultReceiver() throws CouldNotReceiveResultException {
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier9), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier9), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier8), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier7, expectedIdentifier8), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier8), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier5, expectedIdentifier6), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier5, expectedIdentifier6), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier5, expectedIdentifier6), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier5, expectedIdentifier6), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier6, expectedIdentifier7), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier6, expectedIdentifier7), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier8), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier4, expectedIdentifier5, expectedIdentifier7), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier4, expectedIdentifier5, expectedIdentifier8, expectedIdentifier9), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier1, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier9), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier9), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier5), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8, expectedIdentifier9), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8, expectedIdentifier9), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier9), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier9), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier6), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier6), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier8), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier8), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier8), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier8), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier8), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier6, expectedIdentifier8, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier7), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier7), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier7), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier7), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier7), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier6, expectedIdentifier7), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier6, expectedIdentifier7), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier6), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier8, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier2, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier8, expectedIdentifier9), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier8, expectedIdentifier9), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier8, expectedIdentifier9), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier8), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier6, expectedIdentifier9), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier6, expectedIdentifier9), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier6, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier6), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier6), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier7), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier3, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier5, expectedIdentifier6, expectedIdentifier7), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier5, expectedIdentifier6, expectedIdentifier7), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier5, expectedIdentifier6, expectedIdentifier7), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier5));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9), expectedIdentifier8));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier7), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier7), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier8), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier8), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier8), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier8), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier8), expectedIdentifier7));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier8), expectedIdentifier9));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier7, expectedIdentifier8), expectedIdentifier1));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier7, expectedIdentifier8), expectedIdentifier2));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier7, expectedIdentifier8), expectedIdentifier3));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier7, expectedIdentifier8), expectedIdentifier4));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier7, expectedIdentifier8), expectedIdentifier6));
        verify(fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifier5, expectedIdentifier7, expectedIdentifier8), expectedIdentifier9));

        verifyNoMoreInteractions(fdResultReceiver);
    }

    public void verifyUccResultReceiver() throws CouldNotReceiveResultException {
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier8));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier5, expectedIdentifier7, expectedIdentifier8));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier8));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier5, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier5, expectedIdentifier6, expectedIdentifier7));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier1, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier4, expectedIdentifier5, expectedIdentifier6, expectedIdentifier7));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier7, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier5, expectedIdentifier6));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier6));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier4, expectedIdentifier5, expectedIdentifier7));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier3, expectedIdentifier5, expectedIdentifier6, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier5, expectedIdentifier6));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier4, expectedIdentifier6, expectedIdentifier7, expectedIdentifier8));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier8, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier5, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier7, expectedIdentifier8, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier1, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier7));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier7, expectedIdentifier8));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier9));
        verify(uccResultReceiver).receiveResult(new UniqueColumnCombination(expectedIdentifier1, expectedIdentifier2, expectedIdentifier3, expectedIdentifier4, expectedIdentifier6, expectedIdentifier8));

        verifyNoMoreInteractions(uccResultReceiver);
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
