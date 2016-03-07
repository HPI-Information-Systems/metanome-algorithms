package de.metanome.algorithms.ducc;

import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.UniqueColumnCombinationsAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ducc implements UniqueColumnCombinationsAlgorithm, RelationalInputParameterAlgorithm,
                             BooleanParameterAlgorithm {

  protected static final String INPUT_HANDLE = "Relational Input";
  protected static final String NULL_EQUALS_NULL = "NULL = NULL";
  protected boolean nullEqualsNull = true;
  protected Random random;
  protected RelationalInputGenerator inputGenerator;
  protected UniqueColumnCombinationResultReceiver resultReceiver;

  public Ducc() {
    this.random = new Random();
  }

  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
    ArrayList<ConfigurationRequirement<?>> spec = new ArrayList<>();
    ConfigurationRequirementRelationalInput input = new ConfigurationRequirementRelationalInput(INPUT_HANDLE);
    spec.add(input);

    ConfigurationRequirementBoolean nullEqualsNull = new ConfigurationRequirementBoolean(NULL_EQUALS_NULL);
    Boolean[] defaultNullEqualsNull = new Boolean[1];
    defaultNullEqualsNull[0] = true;
	nullEqualsNull.setDefaultValues(defaultNullEqualsNull);
    nullEqualsNull.setRequired(true);
    spec.add(nullEqualsNull);
    
    return spec;
  }

  @Override
  public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values)
      throws AlgorithmConfigurationException {
    if (identifier.equals(INPUT_HANDLE)) {
      inputGenerator = values[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    RelationalInput input;

    input = inputGenerator.generateNewCopy();

    PLIBuilder pliBuilder = new PLIBuilder(input, this.nullEqualsNull);
    List<PositionListIndex> plis = pliBuilder.getPLIList();

    DuccAlgorithm
        duccAlgorithm =
        new DuccAlgorithm(input.relationName(), input.columnNames(), this.resultReceiver);
    duccAlgorithm.run(plis);
  }

  @Override
  public void setResultReceiver(UniqueColumnCombinationResultReceiver receiver) {
    this.resultReceiver = receiver;
  }

  @Override
  public void setBooleanConfigurationValue(String identifier, Boolean... values)
      throws AlgorithmConfigurationException {
    if (identifier.equals(NULL_EQUALS_NULL)) {
      this.nullEqualsNull = values[0];
    } else {
      throw new AlgorithmConfigurationException("Operation should not be called");
    }
  }

  @Override
  public String getAuthors() {
	return "Jens Ehrlich, Jakob Zwiener, Mandy Roick, Lukas Schulze";
  }

  @Override
  public String getDescription() {
	return "Random Walk-based UCC discovery";
  }
}
