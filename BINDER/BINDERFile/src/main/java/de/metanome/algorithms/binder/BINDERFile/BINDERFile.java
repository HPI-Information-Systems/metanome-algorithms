package de.metanome.algorithms.binder.BINDERFile;

import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithms.binder.core.BINDER;
import de.uni_potsdam.hpi.utils.CollectionUtils;

public class BINDERFile extends BINDER implements InclusionDependencyAlgorithm, FileInputParameterAlgorithm, StringParameterAlgorithm, BooleanParameterAlgorithm {

	public enum Identifier {
		INPUT_GENERATOR, INPUT_ROW_LIMIT, TEMP_FOLDER_PATH, CLEAN_TEMP, DETECT_NARY
	};
	
	@Override
	public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement> configs = new ArrayList<ConfigurationRequirement>(5);
		configs.add(new ConfigurationRequirementFileInput(BINDERFile.Identifier.INPUT_GENERATOR.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
		configs.add(new ConfigurationRequirementString(BINDERFile.Identifier.INPUT_ROW_LIMIT.name()));
		configs.add(new ConfigurationRequirementString(BINDERFile.Identifier.TEMP_FOLDER_PATH.name()));
		configs.add(new ConfigurationRequirementBoolean(BINDERFile.Identifier.CLEAN_TEMP.name()));
		configs.add(new ConfigurationRequirementBoolean(BINDERFile.Identifier.DETECT_NARY.name()));
		return configs;
	}

	@Override
	public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.INPUT_GENERATOR.name().equals(identifier)) {
			this.fileInputGenerator = values;
			
			this.tableNames = new String[values.length];
			for (int i = 0; i < values.length; i++)
				this.tableNames[i] = values[i].getInputFile().getName().split("\\.")[0];
		}
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setResultReceiver(InclusionDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.INPUT_ROW_LIMIT.name().equals(identifier))
			this.inputRowLimit = Integer.parseInt(values[0]);
		else if (BINDERFile.Identifier.TEMP_FOLDER_PATH.name().equals(identifier))
			this.tempFolderPath = values[0];
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	@Override
	public void setBooleanConfigurationValue(String identifier, boolean... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.CLEAN_TEMP.name().equals(identifier))
			this.cleanTemp = values[0];
		else if (BINDERFile.Identifier.DETECT_NARY.name().equals(identifier))
			this.detectNary = values[0];
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	protected void handleUnknownConfiguration(String identifier, String value) throws AlgorithmConfigurationException {
		throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + value);
	}
	
	@Override
	public void execute() throws AlgorithmExecutionException {
		super.execute();
	}
}
