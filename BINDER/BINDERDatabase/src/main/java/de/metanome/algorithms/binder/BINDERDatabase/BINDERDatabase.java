package de.metanome.algorithms.binder.BINDERDatabase;

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
import de.metanome.algorithms.binder.core.BINDER;
import de.uni_potsdam.hpi.dao.DB2DataAccessObject;
import de.uni_potsdam.hpi.dao.MySQLDataAccessObject;
import de.uni_potsdam.hpi.dao.PostgreSQLDataAccessObject;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.FileUtils;

public class BINDERDatabase extends BINDER implements InclusionDependencyAlgorithm, DatabaseConnectionParameterAlgorithm, IntegerParameterAlgorithm, StringParameterAlgorithm, BooleanParameterAlgorithm {

	public enum Database {
		MYSQL, DB2, POSTGRESQL
	}
	
	public enum Identifier {
		INPUT_DATABASE, INPUT_ROW_LIMIT, DATABASE_NAME, DATABASE_TYPE, INPUT_TABLES, TEMP_FOLDER_PATH, CLEAN_TEMP, DETECT_NARY
	};
	
	@Override
	public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement> configs = new ArrayList<ConfigurationRequirement>(8);
		configs.add(new ConfigurationRequirementDatabaseConnection(BINDERDatabase.Identifier.INPUT_DATABASE.name()));
		
		ConfigurationRequirementString databaseName = new ConfigurationRequirementString(BINDERDatabase.Identifier.DATABASE_NAME.name());
		databaseName.setRequired(true);
		configs.add(databaseName); // TODO: take this from the input source
		
		ConfigurationRequirementString databaseType = new ConfigurationRequirementString(BINDERDatabase.Identifier.DATABASE_TYPE.name());
		String[] defaultDatabaseType = new String[1];
		defaultDatabaseType[0] = "MYSQL";
		databaseType.setDefaultValues(defaultDatabaseType);
		databaseType.setRequired(true);
		configs.add(databaseType); // TODO: take this from the input source
		
		ConfigurationRequirementString tableNames = new ConfigurationRequirementString(BINDERDatabase.Identifier.INPUT_TABLES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES);
		tableNames.setRequired(true);
		configs.add(tableNames);
		
		ConfigurationRequirementString tempFolder = new ConfigurationRequirementString(BINDERDatabase.Identifier.TEMP_FOLDER_PATH.name());
		String[] defaultTempFolder = new String[1];
		defaultTempFolder[0] = "BINDER_temp";
		tempFolder.setDefaultValues(defaultTempFolder);
		tempFolder.setRequired(true);
		configs.add(tempFolder);

		ConfigurationRequirementInteger inputRowlimit = new ConfigurationRequirementInteger(BINDERDatabase.Identifier.INPUT_ROW_LIMIT.name());
		inputRowlimit.setRequired(false);
		configs.add(inputRowlimit);
		
		ConfigurationRequirementBoolean cleanTemp = new ConfigurationRequirementBoolean(BINDERDatabase.Identifier.CLEAN_TEMP.name());
		Boolean[] defaultCleanTemp = new Boolean[1];
		defaultCleanTemp[0] = true;
		cleanTemp.setDefaultValues(defaultCleanTemp);
		cleanTemp.setRequired(true);
		configs.add(cleanTemp);
		
		ConfigurationRequirementBoolean detectNary = new ConfigurationRequirementBoolean(BINDERDatabase.Identifier.DETECT_NARY.name());
		Boolean[] defaultDetectNary = new Boolean[1];
		defaultDetectNary[0] = false;
		detectNary.setDefaultValues(defaultDetectNary);
		detectNary.setRequired(true);
		configs.add(detectNary);

		return configs;
	}

	@Override
	public void setDatabaseConnectionGeneratorConfigurationValue(String identifier, DatabaseConnectionGenerator... values) throws AlgorithmConfigurationException {
		if (BINDERDatabase.Identifier.INPUT_DATABASE.name().equals(identifier))
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
		if (BINDERDatabase.Identifier.INPUT_ROW_LIMIT.name().equals(identifier)) {
			if (values.length > 0)
				this.inputRowLimit = values[0].intValue();
		}
		else 
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
		if (BINDERDatabase.Identifier.DATABASE_NAME.name().equals(identifier))
			this.databaseName = values[0];
		else if (BINDERDatabase.Identifier.DATABASE_TYPE.name().equals(identifier)) {
			if (BINDERDatabase.Database.MYSQL.name().equals(values[0]))
				this.dao = new MySQLDataAccessObject();
			else if (BINDERDatabase.Database.DB2.name().equals(values[0]))
				this.dao = new DB2DataAccessObject();
			else if (BINDERDatabase.Database.POSTGRESQL.name().equals(values[0]))
				this.dao = new PostgreSQLDataAccessObject();
			else
				this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
		}
		else if (BINDERDatabase.Identifier.INPUT_TABLES.name().equals(identifier))
			this.tableNames = values;
		else if (BINDERDatabase.Identifier.TEMP_FOLDER_PATH.name().equals(identifier)) {
			if ("".equals(values[0]) || " ".equals(values[0]) || "/".equals(values[0]) || "\\".equals(values[0]) || File.separator.equals(values[0]) || FileUtils.isRoot(new File(values[0])))
				throw new AlgorithmConfigurationException(BINDERDatabase.Identifier.TEMP_FOLDER_PATH + " must not be \"" + values[0] + "\"");
			this.tempFolderPath = values[0] + File.separator + "BINDER_temp" + File.separator;
		}
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
		if (BINDERDatabase.Identifier.CLEAN_TEMP.name().equals(identifier))
			this.cleanTemp = values[0];
		else if (BINDERDatabase.Identifier.DETECT_NARY.name().equals(identifier))
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
