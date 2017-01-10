package de.metanome.algorithms.mvddet;

import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.MultivaluedDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.MultivaluedDependencyResultReceiver;

public class MvDDetector extends MvDDetectorAlgorithm 				// Separating the algorithm implementation and the Metanome interface implementation is good practice
						  implements MultivaluedDependencyAlgorithm, 			// Defines the type of the algorithm, i.e., the result type, for instance, FunctionalDependencyAlgorithm or InclusionDependencyAlgorithm; implementing multiple types is possible
						  			 RelationalInputParameterAlgorithm,	// Defines the input type of the algorithm; relational input is any relational input from files or databases; more specific input specifications are possible
						  			 BooleanParameterAlgorithm,
						  			 IntegerParameterAlgorithm
						  			 {

	public enum Identifier {
		INPUT_GENERATOR,
		PRUNING_TYPE,
		REMOVE_DUPLICATES,
		MARK_UNIQUE_VALUES,
		CONVERT_TO_INT_TUPLES,
		USE_PLIS,
		MAX_LHS_SIZE
	};
	
	private MvDAlgorithmConfig algorithmConfig = new MvDAlgorithmConfig();

	@Override
	public String getAuthors() {
		return "Tim Draeger"; // A string listing the author(s) of this algorithm
	}

	@Override
	public String getDescription() {
		return "An algorithm to find MvDs, bottom-up left-hand-side-first"; // A string briefly describing what this algorithm does
	}
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() { // Tells Metanome which and how many parameters the algorithm needs
		ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();
		conf.add(new ConfigurationRequirementRelationalInput(MvDDetector.Identifier.INPUT_GENERATOR.name()));

		
		ConfigurationRequirementInteger pruningType = new ConfigurationRequirementInteger(MvDDetector.Identifier.PRUNING_TYPE.name());
		Integer[] defaultPruningType = new Integer[1];
		defaultPruningType[0] = Integer.valueOf(5);
		pruningType.setDefaultValues(defaultPruningType);
		pruningType.setRequired(true);
		conf.add(pruningType);
		
//		ConfigurationRequirementInteger maxLhsSize = new ConfigurationRequirementInteger(MvDDetector.Identifier.MAX_LHS_SIZE.name());
//		Integer[] defaultMaxLhsSize = new Integer[1];
//		defaultMaxLhsSize[0] = Integer.valueOf(-1);
//		maxLhsSize.setDefaultValues(defaultMaxLhsSize);
//		maxLhsSize.setRequired(true);
//		conf.add(maxLhsSize);

		ConfigurationRequirementBoolean removeDuplicates = new ConfigurationRequirementBoolean(MvDDetector.Identifier.REMOVE_DUPLICATES.name());
		Boolean[] defaultRemoveDuplicates = new Boolean[1];
		defaultRemoveDuplicates[0] = Boolean.valueOf(true);
		removeDuplicates.setDefaultValues(defaultRemoveDuplicates);
		removeDuplicates.setRequired(true);
		conf.add(removeDuplicates);
		
		ConfigurationRequirementBoolean markUniqueValues = new ConfigurationRequirementBoolean(MvDDetector.Identifier.MARK_UNIQUE_VALUES.name());
		Boolean[] defaultMarkUniqueValues = new Boolean[1];
		defaultMarkUniqueValues[0] = Boolean.valueOf(true);
		markUniqueValues.setDefaultValues(defaultMarkUniqueValues);
		markUniqueValues.setRequired(true);
		conf.add(markUniqueValues);
		
		ConfigurationRequirementBoolean convertToIntTuples = new ConfigurationRequirementBoolean(MvDDetector.Identifier.CONVERT_TO_INT_TUPLES.name());
		Boolean[] defaultConvertToIntTuples = new Boolean[1];
		defaultConvertToIntTuples[0] = Boolean.valueOf(true);
		convertToIntTuples.setDefaultValues(defaultConvertToIntTuples);
		convertToIntTuples.setRequired(true);
		conf.add(convertToIntTuples);
		
		ConfigurationRequirementBoolean usePLIs = new ConfigurationRequirementBoolean(MvDDetector.Identifier.USE_PLIS.name());
		Boolean[] defaultUsePLIs = new Boolean[1];
		defaultUsePLIs[0] = Boolean.valueOf(true);
		usePLIs.setDefaultValues(defaultUsePLIs);
		usePLIs.setRequired(true);
		conf.add(usePLIs);
		
		return conf;
	}

	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values) throws AlgorithmConfigurationException {
		if (MvDDetector.Identifier.PRUNING_TYPE.name().equals(identifier) && values[0] <= 5 && values[0] >= 0)
			this.algorithmConfig.setPruningType(values[0]);
//		else if (MvDDetector.Identifier.MAX_LHS_SIZE.name().equals(identifier))
//			this.algorithmConfig.setMaxLhsSize(values[0]);
		else
			this.handleUnknownConfiguration(identifier, values);
	}

	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
		if (MvDDetector.Identifier.REMOVE_DUPLICATES.name().equals(identifier))
			this.algorithmConfig.setRemoveDuplicates(values[0].booleanValue());
		else if (MvDDetector.Identifier.MARK_UNIQUE_VALUES.name().equals(identifier))
			this.algorithmConfig.setMarkUniqueValues(values[0].booleanValue());
		else if (MvDDetector.Identifier.CONVERT_TO_INT_TUPLES.name().equals(identifier))
			this.algorithmConfig.setConvertToIntTuples(values[0].booleanValue());
		else if (MvDDetector.Identifier.USE_PLIS.name().equals(identifier))
			this.algorithmConfig.setUsePLIs(values[0].booleanValue());
		else
			this.handleUnknownConfiguration(identifier, values);
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
		if (!MvDDetector.Identifier.INPUT_GENERATOR.name().equals(identifier))
			this.handleUnknownConfiguration(identifier, values);
		this.inputGenerator = values[0];
	}

	@Override
	public void setResultReceiver(MultivaluedDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void execute() throws AlgorithmExecutionException {
		super.algorithmConfig = this.algorithmConfig;
		super.execute();
	}

	private void handleUnknownConfiguration(String identifier, Object[] values) throws AlgorithmConfigurationException {
		throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> [" + concat(values, ",") + "]");
	}
	
	private static String concat(Object[] objects, String separator) {
		if (objects == null)
			return "";
		
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < objects.length; i++) {
			buffer.append(objects[i].toString());
			if ((i + 1) < objects.length)
				buffer.append(separator);
		}
		return buffer.toString();
	}
}
