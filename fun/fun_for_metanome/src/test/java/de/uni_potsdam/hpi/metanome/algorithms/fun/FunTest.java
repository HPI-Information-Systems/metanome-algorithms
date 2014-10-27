package de.uni_potsdam.hpi.metanome.algorithms.fun;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.AbaloneFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.AlgorithmTestFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.BridgesFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.FDmineFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.FDminimizerShadowedFDFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.ShadowedSuperSetFixture;

public class FunTest {
	protected Fun algorithm;
	
	@Before
	public void setUp() throws Exception {
		algorithm = new Fun();
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link Fun#getConfigurationRequirements()}
	 * 
	 * Fun should request an csv input file.
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
        algorithm.setRelationalInputConfigurationValue(Fun.INPUT_FILE_TAG, expectedInputGenerator);

        // Check result
        assertSame(expectedInputGenerator, algorithm.inputGenerator);
	}

	@Test 
	public void testExecute() throws InputGenerationException, InputIterationException, CouldNotReceiveResultException {
		// Setup
		AlgorithmTestFixture fixture = new AlgorithmTestFixture();
        algorithm.setRelationalInputConfigurationValue(Fun.INPUT_FILE_TAG, fixture.getInputGenerator());
        algorithm.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
        // Execute functionality
		algorithm.execute();
		
		// Check Results
		fixture.verifyFunctionalDependencyResultReceiver();
	}
	
	@Test 
	public void testExecuteFDFixture() throws InputGenerationException, InputIterationException, CouldNotReceiveResultException {
		// Setup
		FDmineFixture fixture = new FDmineFixture();
        algorithm.setRelationalInputConfigurationValue(Fun.INPUT_FILE_TAG, fixture.getInputGenerator());
        algorithm.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
        // Execute functionality
		algorithm.execute();
		
		// Check Results
		fixture.verifyFunctionalDependencyResultReceiver();
	}


	/**
	 * Test method for {@link Fun#setResultReceiver(FunctionalDependencyResultReceiver)}
	 * 
	 * The resultReceiver should be set.
	 */
	@Test
	public void testSetResultReceiverFunctionalDependencyResultReceiver() {
		// Setup
		// Expected values
		FunctionalDependencyResultReceiver expectedResultReceiver = mock(FunctionalDependencyResultReceiver.class);
		
		// Execute functionality 
		algorithm.setResultReceiver(expectedResultReceiver);	
		
		// Check result
		assertSame(expectedResultReceiver, algorithm.resultReceiver);
	}
	
	@Test
	public void testUCCMinimizeFixture() throws InputGenerationException, InputIterationException, CouldNotReceiveResultException {
		// Setup
		FDminimizerShadowedFDFixture fixture = new FDminimizerShadowedFDFixture();
        algorithm.setRelationalInputConfigurationValue(Fun.INPUT_FILE_TAG, fixture.getInputGenerator());
        algorithm.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
        // Execute functionality
		algorithm.execute();
		
		// Check Results
		fixture.verifyFunctionalDependencyResultReceiver();
	}

    @Test
    public void testAbaloneFixture() throws CouldNotReceiveResultException, InputGenerationException, InputIterationException, UnsupportedEncodingException, FileNotFoundException, URISyntaxException, AlgorithmConfigurationException {
        AbaloneFixture fixture = new AbaloneFixture();
        algorithm.setRelationalInputConfigurationValue(Fun.INPUT_FILE_TAG, fixture.getInputGenerator());
        algorithm.setResultReceiver(fixture.getFdResultReceiver());
        // Execute functionality
        algorithm.execute();

        // Check Results
        fixture.verifyFdResultReceiver();
    }

    @Test
    public void testBridgesFixture() throws CouldNotReceiveResultException, InputGenerationException, InputIterationException, UnsupportedEncodingException, FileNotFoundException, AlgorithmConfigurationException {
        BridgesFixture fixture = new BridgesFixture();
        algorithm.setRelationalInputConfigurationValue(Fun.INPUT_FILE_TAG, fixture.getInputGenerator());
        algorithm.setResultReceiver(fixture.getFdResultReceiver());
        // Execute functionality
        algorithm.execute();

        // Check Results
        fixture.verifyFunctionalDependencyResultReceiver();
    }
    
    @Test
	public void testShadowedFds() throws InputGenerationException, InputIterationException, CouldNotReceiveResultException {
		// Setup
    	System.out.println("Shadowed Fds");
    	ShadowedSuperSetFixture fixture = new ShadowedSuperSetFixture();
        algorithm.setRelationalInputConfigurationValue(Fun.INPUT_FILE_TAG, fixture.getInputGenerator());
        algorithm.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
        // Execute functionality
		algorithm.execute();
		
		// Check Results
		fixture.verifyFunctionalDependencyResultReceiver();
	}
	
}
