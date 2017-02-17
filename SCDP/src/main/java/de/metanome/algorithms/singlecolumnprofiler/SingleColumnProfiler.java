package de.metanome.algorithms.singlecolumnprofiler;

import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BasicStatisticsAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;


public class SingleColumnProfiler extends SingleColumnProfilerAlgorithm
    implements BasicStatisticsAlgorithm, RelationalInputParameterAlgorithm, BooleanParameterAlgorithm {

  public enum Identifier {
    INPUT_GENERATOR, NO_AGGREGATION
  };

  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
    ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();
    conf.add(new ConfigurationRequirementRelationalInput(
        SingleColumnProfiler.Identifier.INPUT_GENERATOR.name()));
    ConfigurationRequirementBoolean noAggregation = new ConfigurationRequirementBoolean(
            SingleColumnProfiler.Identifier.NO_AGGREGATION.name()
    );
    noAggregation.setRequired(false);
    noAggregation.setDefaultValues(new Boolean[]{this.isNotAggregating});
    conf.add(noAggregation);
    // conf.add(new
    // ConfigurationRequirementRelationalInput(MyIndDetector.Identifier.INPUT_GENERATOR.name(),
    // ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES)); // For IND discovery, the number of
    // inputs is arbitrary
    // conf.add(new
    // ConfigurationRequirementInteger(MyOdDetector.Identifier.IMPORTANT_PARAMETER.name())); // The
    // algorithm can ask the user for other parameters if needed. If so, then implement the
    // Integer/String/BooleanParameterAlgorithm interfaces as well
    return conf;
  }

  @Override
  public void setResultReceiver(BasicStatisticsResultReceiver resultReceiver) {
    this.resultReceiver = resultReceiver;
  }

  @Override
  public void setRelationalInputConfigurationValue(String identifier,
      RelationalInputGenerator... values) throws AlgorithmConfigurationException {
    if (!SingleColumnProfiler.Identifier.INPUT_GENERATOR.name().equals(identifier))
      throw new AlgorithmConfigurationException(
          "Input generator does not match the expected identifier: " + identifier + " (given) but "
              + SingleColumnProfiler.Identifier.INPUT_GENERATOR.name() + " (expected)");
    this.inputGenerator = values[0];
  }

  @Override
  public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
    if (Identifier.NO_AGGREGATION.name().equalsIgnoreCase(identifier)) {
      this.isNotAggregating = values[0];
    } else {
      throw new IllegalArgumentException(String.format("Unknown configuration identifier: %s.", identifier));
    }
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    super.execute();
  }

  @Override
  public String getAuthors() {
    return "Hazar Harmouch";
  }

  @Override
  public String getDescription() {
    return "Basic Statistics discovery";
  }

}

