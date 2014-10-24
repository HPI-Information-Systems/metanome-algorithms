package de.metanome.algorithms.tane.algorithm_helper.test_helper;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithms.tane.algorithm_helper.test_helper.fixtures.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class AlgorithmTester {
    protected FunctionalDependencyAlgorithm algo;

    @Before
    public abstract void setUp() throws Exception;

    @After
    public void tearDown() throws Exception {
    }

    protected abstract void executeAndVerifyWithFixture(AbstractAlgorithmTestFixture fixture)
            throws AlgorithmExecutionException;

    @Test
    public void testExecute1() throws AlgorithmExecutionException {
        AlgorithmTestFixture1 fixture = new AlgorithmTestFixture1();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute2() throws AlgorithmExecutionException {
        //Setup
        AlgorithmTestFixture2 fixture = new AlgorithmTestFixture2();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute3() throws AlgorithmExecutionException {
        AlgorithmTestFixture3 fixture = new AlgorithmTestFixture3();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute4() throws AlgorithmExecutionException {
        AlgorithmTestFixture4 fixture = new AlgorithmTestFixture4();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute5() throws AlgorithmExecutionException {
        AlgorithmTestFixture5 fixture = new AlgorithmTestFixture5();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute6() throws AlgorithmExecutionException {
        AlgorithmTestFixture6 fixture = new AlgorithmTestFixture6();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute7() throws AlgorithmExecutionException {
        AlgorithmTestFixture7 fixture = new AlgorithmTestFixture7();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute8() throws AlgorithmExecutionException {
        AlgorithmTestFixture8 fixture = new AlgorithmTestFixture8();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute9() throws AlgorithmExecutionException {
        AlgorithmTestFixture9 fixture = new AlgorithmTestFixture9();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute10() throws AlgorithmExecutionException {
        AlgorithmTestFixture10 fixture = new AlgorithmTestFixture10();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute11() throws AlgorithmExecutionException {
        AlgorithmTestFixture11 fixture = new AlgorithmTestFixture11();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute12() throws AlgorithmExecutionException {
        AlgorithmTestFixture12 fixture = new AlgorithmTestFixture12();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute13() throws AlgorithmExecutionException {
        AlgorithmTestFixture13 fixture = new AlgorithmTestFixture13();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute14() throws AlgorithmExecutionException {
        AlgorithmTestFixture14 fixture = new AlgorithmTestFixture14();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute15() throws AlgorithmExecutionException {
        AlgorithmTestFixture15 fixture = new AlgorithmTestFixture15();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute16() throws AlgorithmExecutionException {
        AlgorithmTestFixture16 fixture = new AlgorithmTestFixture16();
        executeAndVerifyWithFixture(fixture);
    }

    @Test
    public void testExecute17() throws AlgorithmExecutionException {
        AlgorithmTestFixture17 fixture = new AlgorithmTestFixture17();
        executeAndVerifyWithFixture(fixture);
    }
}
