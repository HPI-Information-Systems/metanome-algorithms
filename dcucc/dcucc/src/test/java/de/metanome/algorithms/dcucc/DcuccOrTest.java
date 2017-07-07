package de.metanome.algorithms.dcucc;

import de.metanome.algorithms.test_helper.fixtures.AbaloneFixture;
import de.metanome.algorithms.test_helper.fixtures.AlgorithmTestFixture;
import de.metanome.algorithms.test_helper.fixtures.BridgesFixture;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jens Ehrlich
 */
public class DcuccOrTest {

  Dcucc algorithm;

  @Before
  public void setUp() throws Exception {
    algorithm = new Dcucc();
    algorithm.conditionLatticeTraverser = new OrConditionTraverser(algorithm);
  }

  @Test
  public void testAlgorithmFixtureExecute4OrConditions() throws Exception {
    //Setup
    AlgorithmTestFixture fixture = new AlgorithmTestFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getConditionalUniqueResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, false);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 4);
    //Execute
    algorithm.execute();

    //verify result
    fixture.verifyConditionalUniqueColumnCombinationFor4OrConditions();
  }

  @Test
  public void testAbaloneFixtureExecute() throws Exception {
    //Setup
    AbaloneFixture fixture = new AbaloneFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getCUCCResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, true);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 98);
    //Execute
    algorithm.execute();

    //verify result
    //fixture.verifyConditionalUniqueColumnCombinationFor4();
  }

  @Test
  public void testBridgesFixtureExecute() throws Exception {
    //Setup
    BridgesFixture fixture = new BridgesFixture();
    algorithm
        .setRelationalInputConfigurationValue(Dcucc.INPUT_FILE_TAG, fixture.getInputGenerator());
    algorithm.setResultReceiver(fixture.getCUCCResultReceiver());
    algorithm.setBooleanConfigurationValue(Dcucc.PERCENTAGE_TAG, true);
    algorithm.setIntegerConfigurationValue(Dcucc.FREQUENCY_TAG, 90);
    //Execute
    algorithm.execute();

    //verify result
    fixture.verifyConditionalUniqueColumnCombinationOr90();
  }
}
