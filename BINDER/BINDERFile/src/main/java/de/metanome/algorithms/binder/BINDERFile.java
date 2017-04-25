package de.metanome.algorithms.binder;

import java.io.File;
import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithms.binder.core.BINDER;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.FileUtils;

public class BINDERFile extends BINDER implements InclusionDependencyAlgorithm, RelationalInputParameterAlgorithm, IntegerParameterAlgorithm, StringParameterAlgorithm, BooleanParameterAlgorithm {

	public enum Identifier {
		INPUT_FILES, INPUT_ROW_LIMIT, TEMP_FOLDER_PATH, CLEAN_TEMP, DETECT_NARY, MAX_NARY_LEVEL, FILTER_KEY_FOREIGNKEYS, NUM_BUCKETS_PER_COLUMN, MEMORY_CHECK_FREQUENCY, MAX_MEMORY_USAGE_PERCENTAGE
	};
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<ConfigurationRequirement<?>>(5);
		configs.add(new ConfigurationRequirementRelationalInput(BINDERFile.Identifier.INPUT_FILES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
		
		ConfigurationRequirementString tempFolder = new ConfigurationRequirementString(BINDERFile.Identifier.TEMP_FOLDER_PATH.name());
		String[] defaultTempFolder = new String[1];
		defaultTempFolder[0] = this.tempFolderPath;
		tempFolder.setDefaultValues(defaultTempFolder);
		tempFolder.setRequired(true);
		configs.add(tempFolder);
		
		ConfigurationRequirementInteger inputRowLimit = new ConfigurationRequirementInteger(BINDERFile.Identifier.INPUT_ROW_LIMIT.name());
		Integer[] defaultInputRowLimit = { Integer.valueOf(this.inputRowLimit) };
		inputRowLimit.setDefaultValues(defaultInputRowLimit);
		inputRowLimit.setRequired(false);
		configs.add(inputRowLimit);
		
		ConfigurationRequirementInteger maxNaryLevel = new ConfigurationRequirementInteger(BINDERFile.Identifier.MAX_NARY_LEVEL.name());
		Integer[] defaultMaxNaryLevel = { Integer.valueOf(this.maxNaryLevel) };
		maxNaryLevel.setDefaultValues(defaultMaxNaryLevel);
		maxNaryLevel.setRequired(false);
		configs.add(maxNaryLevel);
		
		ConfigurationRequirementInteger numBucketsPerColumn = new ConfigurationRequirementInteger(BINDERFile.Identifier.NUM_BUCKETS_PER_COLUMN.name());
		Integer[] defaultNumBucketsPerColumn = { Integer.valueOf(this.numBucketsPerColumn) };
		numBucketsPerColumn.setDefaultValues(defaultNumBucketsPerColumn);
		numBucketsPerColumn.setRequired(true);
		configs.add(numBucketsPerColumn);

		ConfigurationRequirementInteger memoryCheckFrequency = new ConfigurationRequirementInteger(BINDERFile.Identifier.MEMORY_CHECK_FREQUENCY.name());
		Integer[] defaultMemoryCheckFrequency = { Integer.valueOf(this.memoryCheckFrequency) };
		memoryCheckFrequency.setDefaultValues(defaultMemoryCheckFrequency);
		memoryCheckFrequency.setRequired(true);
		configs.add(memoryCheckFrequency);

		ConfigurationRequirementInteger maxMemoryUsagePercentage = new ConfigurationRequirementInteger(BINDERFile.Identifier.MAX_MEMORY_USAGE_PERCENTAGE.name());
		Integer[] defaultMaxMemoryUsagePercentage = { Integer.valueOf(this.maxMemoryUsagePercentage) };
		maxMemoryUsagePercentage.setDefaultValues(defaultMaxMemoryUsagePercentage);
		maxMemoryUsagePercentage.setRequired(true);
		configs.add(maxMemoryUsagePercentage);
		
		ConfigurationRequirementBoolean cleanTemp = new ConfigurationRequirementBoolean(BINDERFile.Identifier.CLEAN_TEMP.name());
		Boolean[] defaultCleanTemp = new Boolean[1];
		defaultCleanTemp[0] = Boolean.valueOf(this.cleanTemp);
		cleanTemp.setDefaultValues(defaultCleanTemp);
		cleanTemp.setRequired(true);
		configs.add(cleanTemp);
		
		ConfigurationRequirementBoolean detectNary = new ConfigurationRequirementBoolean(BINDERFile.Identifier.DETECT_NARY.name());
		Boolean[] defaultDetectNary = new Boolean[1];
		defaultDetectNary[0] = Boolean.valueOf(this.detectNary);
		detectNary.setDefaultValues(defaultDetectNary);
		detectNary.setRequired(true);
		configs.add(detectNary);

		ConfigurationRequirementBoolean filterKeyForeignkeys = new ConfigurationRequirementBoolean(BINDERFile.Identifier.FILTER_KEY_FOREIGNKEYS.name());
		Boolean[] defaultFilterKeyForeignkeys = new Boolean[1];
		defaultFilterKeyForeignkeys[0] = Boolean.valueOf(this.filterKeyForeignkeys);
		filterKeyForeignkeys.setDefaultValues(defaultFilterKeyForeignkeys);
		filterKeyForeignkeys.setRequired(true);
		configs.add(filterKeyForeignkeys);
		
		return configs;
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.INPUT_FILES.name().equals(identifier)) {
			this.fileInputGenerator = values;
			
			this.tableNames = new String[values.length];
			RelationalInput input = null;
			for (int i = 0; i < values.length; i++) {
				try {
					input = values[i].generateNewCopy();
					this.tableNames[i] = input.relationName();
				}
				catch (InputGenerationException e) {
					throw new AlgorithmConfigurationException(e.getMessage());
				}
				finally {
					FileUtils.close(input);
				}
			}
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
			if (values.length > 0)
				this.maxNaryLevel = values[0].intValue();
		}
		else if (BINDERFile.Identifier.NUM_BUCKETS_PER_COLUMN.name().equals(identifier)) {
			if (values[0].intValue() <= 0)
				throw new AlgorithmConfigurationException(BINDERFile.Identifier.NUM_BUCKETS_PER_COLUMN.name() + " must be greater than 0!");
			this.numBucketsPerColumn = values[0].intValue();
		}
		else if (BINDERFile.Identifier.MEMORY_CHECK_FREQUENCY.name().equals(identifier)) {
			if (values[0].intValue() <= 0)
				throw new AlgorithmConfigurationException(BINDERFile.Identifier.MEMORY_CHECK_FREQUENCY.name() + " must be greater than 0!");
			this.memoryCheckFrequency = values[0].intValue();
		}
		else if (BINDERFile.Identifier.MAX_MEMORY_USAGE_PERCENTAGE.name().equals(identifier)) {
			if (values[0].intValue() <= 0)
				throw new AlgorithmConfigurationException(BINDERFile.Identifier.MAX_MEMORY_USAGE_PERCENTAGE.name() + " must be greater than 0!");
			this.maxMemoryUsagePercentage = values[0].intValue();
		}
		else 
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.TEMP_FOLDER_PATH.name().equals(identifier)) {
			if ("".equals(values[0]) || " ".equals(values[0]) || "/".equals(values[0]) || "\\".equals(values[0]) || File.separator.equals(values[0]) || FileUtils.isRoot(new File(values[0])))
				throw new AlgorithmConfigurationException(BINDERFile.Identifier.TEMP_FOLDER_PATH + " must not be \"" + values[0] + "\"");
			this.tempFolderPath = values[0];
		}
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
		if (BINDERFile.Identifier.CLEAN_TEMP.name().equals(identifier))
			this.cleanTemp = values[0].booleanValue();
		else if (BINDERFile.Identifier.DETECT_NARY.name().equals(identifier))
			this.detectNary = values[0].booleanValue();
		else if (BINDERFile.Identifier.FILTER_KEY_FOREIGNKEYS.name().equals(identifier))
			this.filterKeyForeignkeys = values[0].booleanValue();
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
	
	@Override
	public String getAuthors() {
		return this.getAuthorName();
	}

	@Override
	public String getDescription() {
		return this.getDescriptionText();
	}
}
