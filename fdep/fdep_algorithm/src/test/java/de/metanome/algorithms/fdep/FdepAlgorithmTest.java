package de.metanome.algorithms.fdep;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.tane.algorithm_helper.test_helper.AlgorithmTester;
import de.metanome.algorithms.tane.algorithm_helper.test_helper.fixtures.AbstractAlgorithmTestFixture;
import org.junit.After;
import org.junit.Before;

public class FdepAlgorithmTest extends AlgorithmTester {
    private FdepAlgorithm algo;

    @Before
    public void setUp() throws Exception {
        this.algo = new FdepAlgorithm();
    }

    @After
    public void tearDown() throws Exception {
    }

    protected void executeAndVerifyWithFixture(AbstractAlgorithmTestFixture fixture)
            throws AlgorithmExecutionException {
        this.algo.setRelationalInputConfigurationValue(FdepAlgorithm.INPUT_TAG, fixture.getInputGenerator());
        this.algo.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
        // Execute functionality
        this.algo.execute();

        // Check Results
        fixture.verifyFunctionalDependencyResultReceiver();
    }
}
