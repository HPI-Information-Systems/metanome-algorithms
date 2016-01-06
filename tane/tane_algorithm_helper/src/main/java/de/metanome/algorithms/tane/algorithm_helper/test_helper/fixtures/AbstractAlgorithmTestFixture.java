package de.metanome.algorithms.tane.algorithm_helper.test_helper.fixtures;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractAlgorithmTestFixture {
    protected ImmutableList<String> columnNames;// = ImmutableList.of("A", "B", "C");
    protected int numberOfColumns;// = 3;
    protected String relationName = "TEST_TABLE";
    protected List<ImmutableList<String>> table = new LinkedList<ImmutableList<String>>();
    protected FunctionalDependencyResultReceiver fdResultReceiver = mock(FunctionalDependencyResultReceiver.class);

    public FileInputGenerator getInputGenerator() throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
        FileInputGenerator inputGenerator = mock(FileInputGenerator.class);
        RelationalInput input = this.getRelationalInput();
        when(inputGenerator.generateNewCopy())
                .thenReturn(input);
        return inputGenerator;
    }

    public FunctionalDependencyResultReceiver getFunctionalDependencyResultReceiver() {
        return this.fdResultReceiver;
    }

    public abstract RelationalInput getRelationalInput() throws InputIterationException;

    public abstract void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException;
}
