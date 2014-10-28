package de.uni_potsdam.hpi.metanome.algorithms.fdmine;

import static junit.framework.TestCase.assertSame;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.AlgorithmTestFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.FDmineFixture;

public class FdMineTest {

    FdMine algorithm;

    @Before
    public void setUp() {
        this.algorithm = new FdMine();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link FdMine#getConfigurationRequirements()}
     * <p/>
     * FdMine should request an csv input file.
     */
    @Test
    public void testGetConfigurationRequirements() {
        // Execute functionality
        // Check result
        assertThat(algorithm.getConfigurationRequirements(), hasItem(isA(ConfigurationRequirementRelationalInput.class)));
    }

    @Test
    public void testSetConfigurationValueStringRelationalInputGenerator() {
        // Setup
        // Expected values
        RelationalInputGenerator expectedInputGenerator = mock(RelationalInputGenerator.class);

        // Execute functionality
        algorithm.setRelationalInputConfigurationValue(FdMine.INPUT_FILE_TAG, expectedInputGenerator);

        // Check result
        assertSame(expectedInputGenerator, algorithm.inputGenerator);
    }

    @Test
    public void testExecute() throws AlgorithmExecutionException {
        //Setup
        AlgorithmTestFixture fixture = new AlgorithmTestFixture();
        algorithm.setRelationalInputConfigurationValue(FdMine.INPUT_FILE_TAG, fixture.getInputGenerator());
        algorithm.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
        // Execute functionality
        algorithm.execute();

        // Check Results
        fixture.verifyFunctionalDependencyResultReceiverForFDMine();
    }

    @Test
    public void testExecuteFDFixture() throws InputGenerationException, InputIterationException, CouldNotReceiveResultException {
        // Setup
        FDmineFixture fixture = new FDmineFixture();
        algorithm.setRelationalInputConfigurationValue(FdMine.INPUT_FILE_TAG, fixture.getInputGenerator());
        algorithm.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
        // Execute functionality
        algorithm.execute();

        // Check Results
        fixture.verifyFunctionalDependencyResultReceiver();
    }

    /**
     * Test method for {@link FdMine#setResultReceiver(FunctionalDependencyResultReceiver)}
     * <p/>
     * The resultReceiver should be set.
     */
    @Test
    public void testSetResultReceiver() {
        // Setup
        // Expected values
        FunctionalDependencyResultReceiver expectedResultReceiver = mock(FunctionalDependencyResultReceiver.class);

        // Execute functionality
        algorithm.setResultReceiver(expectedResultReceiver);

        // Check result
        assertSame(expectedResultReceiver, algorithm.resultReceiver);
    }

}
