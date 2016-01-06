package de.uni_potsdam.hpi.metanome.algorithms.fun;

import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.AlgorithmTestFixture;
import de.uni_potsdam.hpi.metanome.algorithms.test_helper.fixtures.FunFastCountFixture;

public class FunAlgorithmTest {

	/**
	 * Test method for {@link FunAlgorithm#generateCandidates(List)}
	 * 
	 * Should generate the correct candidates:
	 * 
	 * Input l2:
	 * 
	 * 0011
	 * 0110
	 * 1100
	 * 1010
	 * 
	 * Generates candidates:
	 * 
	 * 1011 futile 1001 missing
	 * 0111 futile 0101 missing
	 * 1110 candidate
	 * 1101 futile 0101 missing
	 * 
	 * Result l3:
	 * 
	 * 1110
	 */
	@Test
	public void testGenerateCandidate() {
		// Setup
		FixtureCandidateGeneration fixture = new FixtureCandidateGeneration();
		List<FunQuadruple> l2 = fixture.getL2();
		FunAlgorithm fun = fixture.getFunAlgorithmMockedAddPliGenerate();
		
		// Execute functionality
		// Check result
		assertThat(fun.generateCandidates(l2), IsIterableContainingInAnyOrder.containsInAnyOrder(fixture.getExpectedL3Array()));
		// level 2 should be unchanged
		assertThat(l2, IsIterableContainingInAnyOrder.containsInAnyOrder(fixture.getExpectedL2Array()));
	}
	
	/**
	 * TODO docs
	 * 
	 * @throws CouldNotReceiveResultException 
	 * @throws InputIterationException 
	 * @throws InputGenerationException 
	 * @throws AlgorithmConfigurationException 
	 */
	@Test
	public void testYieldsAllFDsAndUCCs() throws InputIterationException, CouldNotReceiveResultException, InputGenerationException, AlgorithmConfigurationException {
		// Setup
		AlgorithmTestFixture fixture = new AlgorithmTestFixture();
		RelationalInput relationalInput = fixture.getInputGenerator().generateNewCopy();
		
		FunAlgorithm funAlgorithm = new FunAlgorithm(
				relationalInput.relationName(), 
				relationalInput.columnNames(), 
				fixture.getFunctionalDependencyResultReceiver(), 
				fixture.getUniqueColumnCombinationResultReceiver());
		// Execute functionality
		funAlgorithm.run(new PLIBuilder(relationalInput).getPLIList());
		
		// Check Results
		fixture.verifyFunctionalDependencyResultReceiver();
		fixture.verifyUniqueColumnCombinationResultReceiver();
	}
	
	/**
	 * TODO docs
	 * 
	 * @throws CouldNotReceiveResultException
	 * @throws InputGenerationException
	 * @throws InputIterationException
	 * @throws AlgorithmConfigurationException 
	 */
	@Test
	public void testFunFastCount() throws CouldNotReceiveResultException, InputGenerationException, InputIterationException, AlgorithmConfigurationException {
		// Setup
		FunFastCountFixture fixture = new FunFastCountFixture();
		RelationalInput relationalInput = fixture.getInputGenerator().generateNewCopy();
		
		FunAlgorithm funAlgorithm = new FunAlgorithm(
				relationalInput.relationName(),
				relationalInput.columnNames(),
				fixture.getFunctionalDependencyResultReceiver());
		// Execute functionality
		funAlgorithm.run(new PLIBuilder(relationalInput).getPLIList());
		
		// Check Results
		fixture.verifyFunctionalDependencyResultReceiver();
		
	}
}
