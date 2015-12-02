package de.metanome.algorithms.ducc;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithms.ducc.test_helper.fixtures.AlgorithmTestFixture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DuccTest {

  protected Ducc ducc;

  @Before
  public void setUp() throws Exception {
    ducc = new Ducc();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetConfigurationRequirements() {
    ConfigurationRequirementRelationalInput
        expectedSpecification1 =
        new ConfigurationRequirementRelationalInput(Ducc.INPUT_HANDLE);

    ConfigurationRequirementBoolean
        expectedSpecification2 =
        new ConfigurationRequirementBoolean(Ducc.NULL_EQUALS_NULL);

    List<ConfigurationRequirement<?>> spec = ducc.getConfigurationRequirements();
    assertEquals(2, spec.size());
    assertEquals(expectedSpecification1.getIdentifier(), spec.get(0).getIdentifier());
    assertEquals(expectedSpecification2.getIdentifier(), spec.get(1).getIdentifier());
  }

  @Test
  public void testSetConfigurationValueStringSimpleRelationalInputGenerator()
      throws AlgorithmConfigurationException {
    RelationalInputGenerator expectedInputGenerator = mock(RelationalInputGenerator.class);

    ducc.setRelationalInputConfigurationValue(Ducc.INPUT_HANDLE, expectedInputGenerator);
    assertEquals(expectedInputGenerator, ducc.inputGenerator);
  }

  @Test
  public void testSetConfigurationValueNullEqualsNull()
      throws AlgorithmConfigurationException {
    boolean nullEqualsNull = false;

    ducc.setBooleanConfigurationValue(Ducc.NULL_EQUALS_NULL, nullEqualsNull);
    assertEquals(nullEqualsNull, ducc.nullEqualsNull);
  }


  /**
   * Setting InputGenerator with wrong Identifier should throw Exception
   */
  @Test
  public void testSetConfigurationValueStringSimpleRelationalInputGeneratorFail()
      throws AlgorithmConfigurationException {
    RelationalInputGenerator expectedInputGenerator = mock(RelationalInputGenerator.class);
    try {
      ducc.setRelationalInputConfigurationValue("wrong identifier", expectedInputGenerator);
      fail("UnsupportedOperationException was expected but was not thrown");
    } catch (AlgorithmConfigurationException e) {

    }
    assertEquals(null, ducc.inputGenerator);
  }

  @Test
  public void testExecute()
      throws AlgorithmExecutionException, NoSuchFieldException, SecurityException,
             IllegalArgumentException, IllegalAccessException {
    //Setup
    AlgorithmTestFixture fixture = new AlgorithmTestFixture();
    ducc.setRelationalInputConfigurationValue(Ducc.INPUT_HANDLE, fixture.getInputGenerator());
    ducc.setResultReceiver(fixture.getUniqueColumnCombinationResultReceiver());

    Random random = mock(Random.class);
    when(random.nextDouble()).thenReturn(1d);
    ducc.random = random;

    //Execute functionality
    ducc.execute();

    //Check Results
    fixture.verifyUniqueColumnCombinationResultReceiver();
  }

  /**
   * Test method for {@link Ducc#setResultReceiver(UniqueColumnCombinationResultReceiver)} <p/> The
   * resultReceiver should be set.
   */
  @Test
  public void testSetResultReceiverUniqueColumnCombinationResultReceiver() {
    // Setup
    // Expected values
    UniqueColumnCombinationResultReceiver
        expectedResultReceiver =
        mock(UniqueColumnCombinationResultReceiver.class);

    // Execute functionality
    ducc.setResultReceiver(expectedResultReceiver);

    // Check result
    assertSame(expectedResultReceiver, ducc.resultReceiver);
  }

}
