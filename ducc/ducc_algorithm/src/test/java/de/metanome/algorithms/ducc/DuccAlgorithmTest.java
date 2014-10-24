package de.metanome.algorithms.ducc;

import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithms.ducc.test_helper.fixtures.AlgorithmTestFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Random;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DuccAlgorithmTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testExecute() throws AlgorithmExecutionException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        //Setup
        AlgorithmTestFixture fixture = new AlgorithmTestFixture();
        RelationalInput relationalInput = fixture.getInputGenerator().generateNewCopy();
        Random random = mock(Random.class);
        when(random.nextInt(anyInt())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Integer) args[0] - 1;
            }
        });
        DuccAlgorithm ducc = new DuccAlgorithm(
                relationalInput.relationName(),
                relationalInput.columnNames(),
                fixture.getUniqueColumnCombinationResultReceiver(),
                random);

        //Execute functionality
        ducc.run(new PLIBuilder(relationalInput).getPLIList());

        //Check Results
        fixture.verifyUniqueColumnCombinationResultReceiver();
    }

    @Test
    public void testExecuteWithHoles() throws AlgorithmExecutionException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        //Setup
        DuccTestFixtureWithHoles fixture = new DuccTestFixtureWithHoles();
        Random random = mock(Random.class);
        when(random.nextInt(anyInt())).thenAnswer(new Answer<Integer>() {

            protected int[] randomVariables = {8, 0, 0, 0, 3, 2, 0, 1, 1, 1, 0, 0, 0, 0, 0, 2, 0};
            protected int numberOfInvocations = 0;

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                if (numberOfInvocations < randomVariables.length) {
                    return randomVariables[numberOfInvocations++];
                } else {
                    return 0;
                }
            }
        });
        DuccAlgorithm ducc = new DuccAlgorithm(fixture.relationName, fixture.columnNames, fixture.uniqueColumnCombinationResultReceiver, random);
        RelationalInput relationalInput = fixture.getInputGenerator().generateNewCopy();

        //Execute functionality
        ducc.run(new PLIBuilder(relationalInput).getPLIList());

        //Check Results
        fixture.verifyUniqueColumnCombinationResultReceiver();
    }

    /**
     * This test sets the desired raw key error to a value different to zero. As result, only partial minimal uniques should be found in the fixture when the algorithm is executed.
     *
     * @throws AlgorithmExecutionException
     */
    @Test
    public void testDesiredRawKeyError() throws AlgorithmExecutionException {
        //Setup
        AlgorithmTestFixture fixture = new AlgorithmTestFixture();

        RelationalInput input;

        input = fixture.getInputGenerator().generateNewCopy();

        PLIBuilder pliBuilder = new PLIBuilder(input);
        List<PositionListIndex> plis = pliBuilder.getPLIList();

        DuccAlgorithm duccAlgorithm = new DuccAlgorithm(input.relationName(), input.columnNames(), fixture.getUniqueColumnCombinationResultReceiver());
        duccAlgorithm.setRawKeyError(2);
        //Execute functionality
        duccAlgorithm.run(plis);
        //Check Result
        fixture.verifyPartialUCCResultReceiver();
    }
}
