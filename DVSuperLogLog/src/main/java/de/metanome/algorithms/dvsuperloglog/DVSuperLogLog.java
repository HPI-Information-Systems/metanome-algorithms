package de.metanome.algorithms.dvsuperloglog;

import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BasicStatisticsAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
/** Implementation of SuperLogLog
 ** Reference:
 *   Durand, M., & Flajolet, P. (2003). Loglog counting of large cardinalities. In Algorithms-ESA 2003 (pp. 605-617). Springer Berlin Heidelberg.
 * * @author Hazar.Harmouch
 */

public class DVSuperLogLog extends DVSuperLogLogAlgorithm implements BasicStatisticsAlgorithm, RelationalInputParameterAlgorithm, StringParameterAlgorithm  {

	public enum Identifier {
		INPUT_GENERATOR,
		STANDARD_ERROR
		
	};
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();
		conf.add(new ConfigurationRequirementRelationalInput(DVSuperLogLog.Identifier.INPUT_GENERATOR.name()));
		ConfigurationRequirementString inputstandard_error=new ConfigurationRequirementString(DVSuperLogLog.Identifier.STANDARD_ERROR.name());
        inputstandard_error.setRequired(false);
        String[] Defaults={"0.01"};
        inputstandard_error.setDefaultValues(Defaults);
        conf.add(inputstandard_error);
        
		//conf.add(new ConfigurationRequirementRelationalInput(MyIndDetector.Identifier.INPUT_GENERATOR.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES)); // For IND discovery, the number of inputs is arbitrary
		//conf.add(new ConfigurationRequirementInteger(MyOdDetector.Identifier.IMPORTANT_PARAMETER.name())); // The algorithm can ask the user for other parameters if needed. If so, then implement the Integer/String/BooleanParameterAlgorithm interfaces as well
		return conf;
	}

	@Override
	public void setResultReceiver(BasicStatisticsResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
		if (!DVSuperLogLog.Identifier.INPUT_GENERATOR.name().equals(identifier))
			throw new AlgorithmConfigurationException("Input generator does not match the expected identifier: " + identifier + " (given) but " + DVSuperLogLog.Identifier.INPUT_GENERATOR.name() + " (expected)");
		this.inputGenerator = values[0];
	}

	@Override
	public void execute() throws AlgorithmExecutionException {
		super.execute();
	}

	@Override
    public void setStringConfigurationValue(String identifier, String... values)
        throws AlgorithmConfigurationException {
      if (DVSuperLogLog.Identifier.STANDARD_ERROR.name().equals(identifier))
      {
        if(values!=null && !values[0].equals("") )
        {try{
        double error= Double.parseDouble(values[0]);
        if(error>0 && error<1)
         this.eps=error;
        else
          throw new Exception();
        }catch(Exception ex)
        {throw new AlgorithmConfigurationException("The Standard Error Epsilon should be a positive double in (0, 1) range");}
        
        }
        
        
      }}

  @Override
  public String getAuthors() {
    return "Hazar Harmouch";
  }

  @Override
  public String getDescription() {
    return "SuperLogLog Cardinality Estimator. Durand, M., & Flajolet, P. (2003). Loglog counting of large cardinalities. In Algorithms-ESA 2003 (pp. 605-617). Springer Berlin Heidelberg";
  }

}
