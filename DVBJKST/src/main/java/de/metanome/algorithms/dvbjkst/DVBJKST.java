package de.metanome.algorithms.dvbjkst;

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
/**
 * BJKST algorithm for distinct counting.
 * 
 * Reference: Bar-Yossef, Ziv, et al. "Counting distinct elements in a data stream." Randomization
 * and Approximation Techniques in Computer Science. Springer Berlin Heidelberg, 2002. 1-10.
 * @author Hazar.Harmouch
 */

public class DVBJKST extends DVBJKSTAlgorithm implements BasicStatisticsAlgorithm, RelationalInputParameterAlgorithm, StringParameterAlgorithm {

	public enum Identifier {
		INPUT_GENERATOR,
		RELATIVE_ERROR,
		
		
	};
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();
		conf.add(new ConfigurationRequirementRelationalInput(DVBJKST.Identifier.INPUT_GENERATOR.name()));
		ConfigurationRequirementString inputstandard_error=new ConfigurationRequirementString(DVBJKST.Identifier.RELATIVE_ERROR.name());
		
        inputstandard_error.setRequired(false);
       
        String[] Defaults_error={"0.01"};
        
        inputstandard_error.setDefaultValues(Defaults_error);
        
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
		if (!DVBJKST.Identifier.INPUT_GENERATOR.name().equals(identifier))
			throw new AlgorithmConfigurationException("Input generator does not match the expected identifier: " + identifier + " (given) but " + DVBJKST.Identifier.INPUT_GENERATOR.name() + " (expected)");
		this.inputGenerator = values[0];
	}

	@Override
	public void execute() throws AlgorithmExecutionException {
		super.execute();
	}

	@Override
	  public void setStringConfigurationValue(String identifier, String... values)
	      throws AlgorithmConfigurationException {
	    if (DVBJKST.Identifier.RELATIVE_ERROR.name().equals(identifier))
	    {
	      if(values!=null && !values[0].equals("") )
	      {try{
	       double error= Double.parseDouble(values[0]);
	       if(error>0 && error<1)
	       this.eps=error;
	       else
	         throw new Exception();
	      }catch(Exception ex)
	      {throw new AlgorithmConfigurationException("The relative error Epsilon should be a positive double in (0, 1) range");}
	      
	      }
	      
	      
	    }}


  

  @Override
  public String getAuthors() {
    return "Hazar Harmouch";
  }

  @Override
  public String getDescription() {
    
    return "Bar-Yossef, Ziv, et al. Counting distinct elements in a data stream.Randomization and Approximation Techniques in Computer Science. Springer Berlin Heidelberg, 2002. 1-10.";
  }

  
  


}
