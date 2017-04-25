package de.metanome.algorithms.spider;

import java.io.File;
import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.DatabaseConnectionParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementDatabaseConnection;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithms.spider.core.SPIDER;
import de.uni_potsdam.hpi.dao.DB2DataAccessObject;
import de.uni_potsdam.hpi.dao.MySQLDataAccessObject;
import de.uni_potsdam.hpi.dao.PostgreSQLDataAccessObject;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.FileUtils;

public class SPIDERDatabase extends SPIDER implements InclusionDependencyAlgorithm, DatabaseConnectionParameterAlgorithm, IntegerParameterAlgorithm, StringParameterAlgorithm, BooleanParameterAlgorithm {

	public enum Database {
		MYSQL, DB2, POSTGRESQL, FILE
	}
	
	public enum Identifier {
		INPUT_DATABASE, INPUT_ROW_LIMIT, DATABASE_NAME, DATABASE_TYPE, INPUT_TABLES, TEMP_FOLDER_PATH, CLEAN_TEMP, MEMORY_CHECK_FREQUENCY, MAX_MEMORY_USAGE_PERCENTAGE
	};

	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<ConfigurationRequirement<?>>(7);
		configs.add(new ConfigurationRequirementDatabaseConnection(SPIDERDatabase.Identifier.INPUT_DATABASE.name()));
		
		ConfigurationRequirementString databaseName = new ConfigurationRequirementString(SPIDERDatabase.Identifier.DATABASE_NAME.name());
		databaseName.setRequired(true);
		configs.add(databaseName); // TODO: take this from the input source
		
		ConfigurationRequirementString databaseType = new ConfigurationRequirementString(SPIDERDatabase.Identifier.DATABASE_TYPE.name());
		String[] defaultDatabaseType = new String[1];
		defaultDatabaseType[0] = "MYSQL";
		databaseType.setDefaultValues(defaultDatabaseType);
		databaseType.setRequired(true);
		configs.add(databaseType); // TODO: take this from the input source
		
		ConfigurationRequirementString tableNames = new ConfigurationRequirementString(SPIDERDatabase.Identifier.INPUT_TABLES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES);
		tableNames.setRequired(true);
		configs.add(tableNames);
		
		ConfigurationRequirementString tempFolder = new ConfigurationRequirementString(SPIDERDatabase.Identifier.TEMP_FOLDER_PATH.name());
		String[] defaultTempFolder = new String[1];
		defaultTempFolder[0] = this.tempFolderPath;
		tempFolder.setDefaultValues(defaultTempFolder);
		tempFolder.setRequired(true);
		configs.add(tempFolder);

		ConfigurationRequirementInteger inputRowLimit = new ConfigurationRequirementInteger(SPIDERDatabase.Identifier.INPUT_ROW_LIMIT.name());
		Integer[] defaultInputRowLimit = { Integer.valueOf(this.inputRowLimit) };
		inputRowLimit.setDefaultValues(defaultInputRowLimit);
		inputRowLimit.setRequired(false);
		configs.add(inputRowLimit);

		ConfigurationRequirementInteger memoryCheckFrequency = new ConfigurationRequirementInteger(SPIDERDatabase.Identifier.MEMORY_CHECK_FREQUENCY.name());
		Integer[] defaultMemoryCheckFrequency = { Integer.valueOf(this.memoryCheckFrequency) };
		memoryCheckFrequency.setDefaultValues(defaultMemoryCheckFrequency);
		memoryCheckFrequency.setRequired(true);
		configs.add(memoryCheckFrequency);

		ConfigurationRequirementInteger maxMemoryUsagePercentage = new ConfigurationRequirementInteger(SPIDERDatabase.Identifier.MAX_MEMORY_USAGE_PERCENTAGE.name());
		Integer[] defaultMaxMemoryUsagePercentage = { Integer.valueOf(this.maxMemoryUsagePercentage) };
		maxMemoryUsagePercentage.setDefaultValues(defaultMaxMemoryUsagePercentage);
		maxMemoryUsagePercentage.setRequired(true);
		configs.add(maxMemoryUsagePercentage);
		
		ConfigurationRequirementBoolean cleanTemp = new ConfigurationRequirementBoolean(SPIDERDatabase.Identifier.CLEAN_TEMP.name());
		Boolean[] defaultCleanTemp = new Boolean[1];
		defaultCleanTemp[0] = Boolean.valueOf(this.cleanTemp);
		cleanTemp.setDefaultValues(defaultCleanTemp);
		cleanTemp.setRequired(true);
		configs.add(cleanTemp);
		
		return configs;
	}

	@Override
	public void setDatabaseConnectionGeneratorConfigurationValue(String identifier, DatabaseConnectionGenerator... values) throws AlgorithmConfigurationException {
		if (SPIDERDatabase.Identifier.INPUT_DATABASE.name().equals(identifier))
			this.databaseConnectionGenerator = values[0];
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setResultReceiver(InclusionDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values) throws AlgorithmConfigurationException {
		if (SPIDERDatabase.Identifier.INPUT_ROW_LIMIT.name().equals(identifier)) {
			if (values.length > 0)
				this.inputRowLimit = values[0].intValue();
		}
		else if (SPIDERDatabase.Identifier.MEMORY_CHECK_FREQUENCY.name().equals(identifier)) {
			if (values[0].intValue() <= 0)
				throw new AlgorithmConfigurationException(SPIDERDatabase.Identifier.MEMORY_CHECK_FREQUENCY.name() + " must be greater than 0!");
			this.memoryCheckFrequency = values[0].intValue();
		}
		else if (SPIDERDatabase.Identifier.MAX_MEMORY_USAGE_PERCENTAGE.name().equals(identifier)) {
			if (values[0].intValue() <= 0)
				throw new AlgorithmConfigurationException(SPIDERDatabase.Identifier.MAX_MEMORY_USAGE_PERCENTAGE.name() + " must be greater than 0!");
			this.maxMemoryUsagePercentage = values[0].intValue();
		}
		else 
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
		if (SPIDERDatabase.Identifier.DATABASE_NAME.name().equals(identifier))
			this.databaseName = values[0];
		else if (SPIDERDatabase.Identifier.DATABASE_TYPE.name().equals(identifier)) {
			if (SPIDERDatabase.Database.MYSQL.name().equals(values[0]))
				this.dao = new MySQLDataAccessObject();
			else if (SPIDERDatabase.Database.DB2.name().equals(values[0]))
				this.dao = new DB2DataAccessObject();
			else if (SPIDERDatabase.Database.POSTGRESQL.name().equals(values[0]))
				this.dao = new PostgreSQLDataAccessObject();
			else
				this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
		}
		else if (SPIDERDatabase.Identifier.INPUT_TABLES.name().equals(identifier))
			this.tableNames = values;
		else if (SPIDERDatabase.Identifier.TEMP_FOLDER_PATH.name().equals(identifier)) {
			if ("".equals(values[0]) || " ".equals(values[0]) || "/".equals(values[0]) || "\\".equals(values[0]) || File.separator.equals(values[0]) || FileUtils.isRoot(new File(values[0])))
				throw new AlgorithmConfigurationException(SPIDERDatabase.Identifier.TEMP_FOLDER_PATH + " must not be \"" + values[0] + "\"");
			this.tempFolderPath = values[0];
		}
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
		if (SPIDERDatabase.Identifier.CLEAN_TEMP.name().equals(identifier))
			this.cleanTemp = values[0].booleanValue();
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
