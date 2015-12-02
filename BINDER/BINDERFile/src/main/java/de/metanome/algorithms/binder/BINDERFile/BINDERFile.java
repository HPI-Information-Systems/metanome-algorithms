package de.metanome.algorithms.binder.BINDERFile;

import java.io.File;
import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithms.binder.core.BINDER;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.FileUtils;

public class BINDERFile extends BINDER implements InclusionDependencyAlgorithm, FileInputParameterAlgorithm, IntegerParameterAlgorithm, StringParameterAlgorithm, BooleanParameterAlgorithm {

	public enum Identifier {
		INPUT_FILES, INPUT_ROW_LIMIT, TEMP_FOLDER_PATH, CLEAN_TEMP, DETECT_NARY, MAX_NARY_LEVEL, FILTER_KEY_FOREIGNKEYS
	};
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<>(5);
		configs.add(new ConfigurationRequirementFileInput(BINDERFile.Identifier.INPUT_FILES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
		
		ConfigurationRequirementString tempFolder = new ConfigurationRequirementString(BINDERFile.Identifier.TEMP_FOLDER_PATH.name());
		String[] defaultTempFolder = new String[1];
		defaultTempFolder[0] = "BINDER_temp";
		tempFolder.setDefaultValues(defaultTempFolder);
		tempFolder.setRequired(true);
		configs.add(tempFolder);
		
		ConfigurationRequirementInteger inputRowlimit = new ConfigurationRequirementInteger(BINDERFile.Identifier.INPUT_ROW_LIMIT.name());
		inputRowlimit.setRequired(false);
		configs.add(inputRowlimit);
		
		ConfigurationRequirementBoolean cleanTemp = new ConfigurationRequirementBoolean(BINDERFile.Identifier.CLEAN_TEMP.name());
		Boolean[] defaultCleanTemp = new Boolean[1];
		defaultCleanTemp[0] = true;
		cleanTemp.setDefaultValues(defaultCleanTemp);
		cleanTemp.setRequired(true);
		configs.add(cleanTemp);
		
		ConfigurationRequirementBoolean detectNary = new ConfigurationRequirementBoolean(BINDERFile.Identifier.DETECT_NARY.name());
		Boolean[] defaultDetectNary = new Boolean[1];
		defaultDetectNary[0] = false;
		detectNary.setDefaultValues(defaultDetectNary);
		detectNary.setRequired(true);
		configs.add(detectNary);

		ConfigurationRequirementInteger maxNaryLevel = new ConfigurationRequirementInteger(BINDERFile.Identifier.MAX_NARY_LEVEL.name());
		Integer[] defaultMaxNaryLevel = { Integer.valueOf(-1), Integer.valueOf(0) };
		maxNaryLevel.setDefaultValues(defaultMaxNaryLevel);
		maxNaryLevel.setRequired(false);
		configs.add(maxNaryLevel);

		ConfigurationRequirementBoolean filterKeyForeignkeys = new ConfigurationRequirementBoolean(BINDERFile.Identifier.FILTER_KEY_FOREIGNKEYS.name());
		Boolean[] defaultFilterKeyForeignkeys = new Boolean[1];
		defaultFilterKeyForeignkeys[0] = false;
		filterKeyForeignkeys.setDefaultValues(defaultFilterKeyForeignkeys);
		filterKeyForeignkeys.setRequired(true);
		configs.add(filterKeyForeignkeys);
		
		return configs;
	}

	@Override
	public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.INPUT_FILES.name().equals(identifier)) {
			this.fileInputGenerator = values;
			
			this.tableNames = new String[values.length];
			for (int i = 0; i < values.length; i++)
				this.tableNames[i] = values[i].getInputFile().getName();
		}
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setResultReceiver(InclusionDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.INPUT_ROW_LIMIT.name().equals(identifier)) {
			if (values.length > 0)
				this.inputRowLimit = values[0].intValue();
		}
		else if (BINDERFile.Identifier.MAX_NARY_LEVEL.name().equals(identifier)) {
			if (values.length > 0) {
				this.maxNaryLevel = values[0].intValue();
			}
		}
		else 
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.TEMP_FOLDER_PATH.name().equals(identifier)) {
			if ("".equals(values[0]) || " ".equals(values[0]) || "/".equals(values[0]) || "\\".equals(values[0]) || File.separator.equals(values[0]) || FileUtils.isRoot(new File(values[0])))
				throw new AlgorithmConfigurationException(BINDERFile.Identifier.TEMP_FOLDER_PATH + " must not be \"" + values[0] + "\"");
			this.tempFolderPath = values[0] + File.separator + "BINDER_temp" + File.separator;
		}
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.CLEAN_TEMP.name().equals(identifier))
			this.cleanTemp = values[0];
		else if (BINDERFile.Identifier.DETECT_NARY.name().equals(identifier))
			this.detectNary = values[0];
		else if (BINDERFile.Identifier.FILTER_KEY_FOREIGNKEYS.name().equals(identifier))
			this.filterKeyForeignkeys = values[0];
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
