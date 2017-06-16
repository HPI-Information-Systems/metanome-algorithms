package de.metanome.algorithms.dvhyperloglogplus;

import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BasicStatisticsAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;

import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
/**
* Implementation of HyperLogLogplus 
* Reference: Heule, S., Nunkesser, M., & Hall, A. (2013, March).
* HyperLogLog in practice: algorithmic engineering of a state of the art cardinality estimation
* algorithm. In Proceedings of the 16th International Conference on Extending Database Technology
* (pp. 683-692). ACM.Hyperloglog. Flajolet, P., Fusy, É., Gandouet, O., & Meunier, F. (2008).
* Hyperloglog: the analysis of a near-optimal cardinality estimation algorithm. DMTCS Proceedings,
* (1). 
* * @author Hazar.Harmouch 
*/

public class DVHyperLogLogPlus extends DVHyperLogLogAlgorithmplus implements
    BasicStatisticsAlgorithm, RelationalInputParameterAlgorithm, IntegerParameterAlgorithm {

  public enum Identifier {
    INPUT_GENERATOR, PERCISION, PERCISION_SPARSE

  };

  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
    ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();
    conf.add(new ConfigurationRequirementRelationalInput(
        DVHyperLogLogPlus.Identifier.INPUT_GENERATOR.name()));
    ConfigurationRequirementInteger p =
        new ConfigurationRequirementInteger(DVHyperLogLogPlus.Identifier.PERCISION.name());
    ConfigurationRequirementInteger ps =
        new ConfigurationRequirementInteger(DVHyperLogLogPlus.Identifier.PERCISION_SPARSE.name());
    p.setRequired(true);
    ps.setRequired(true);
    Integer[] Defaultp = {14};
    p.setDefaultValues(Defaultp);
    Integer[] Defaultps = {25};
    ps.setDefaultValues(Defaultps);
    conf.add(p);
    conf.add(ps);
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
    if (!DVHyperLogLogPlus.Identifier.INPUT_GENERATOR.name().equals(identifier))
      throw new AlgorithmConfigurationException(
          "Input generator does not match the expected identifier: " + identifier + " (given) but "
              + DVHyperLogLogPlus.Identifier.INPUT_GENERATOR.name() + " (expected)");
    this.inputGenerator = values[0];
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

    return "Heule, S., Nunkesser, M., & Hall, A. (2013). HyperLogLog in practice: algorithmic engineering of a state of the art cardinality estimation algorithm. In Proceedings of the 16th International Conference on Extending Database Technology";
  }

  @Override
  public void setIntegerConfigurationValue(String identifier, Integer... values)
      throws AlgorithmConfigurationException {
    
    if(DVHyperLogLogPlus.Identifier.PERCISION_SPARSE.name().equals(identifier)){
      if(values!=null && !values[0].equals("") ){
      int ps= values[0];
      if (ps > 32 || ps<0 )  throw new IllegalArgumentException("sp values greater than 32 not supported");
       this.ps=ps;} 
    }else if (DVHyperLogLogPlus.Identifier.PERCISION.name().equals(identifier)) {
          if(values!=null && !values[0].equals("") )
          {
           int p= values[0];
           if ((p < 4) || ((p > this.ps) && (this.ps != 0)))  throw new IllegalArgumentException("p must be between 4 and sp (inclusive)");   
           this.p=p; } }
  }

}
