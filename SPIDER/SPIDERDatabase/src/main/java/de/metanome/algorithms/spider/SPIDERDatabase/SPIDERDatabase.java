package de.metanome.algorithms.spider.SPIDERDatabase;

import java.io.File;
import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.DatabaseConnectionParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementDatabaseConnection;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithms.spider.core.SPIDER;
import de.uni_potsdam.hpi.dao.DB2DataAccessObject;
import de.uni_potsdam.hpi.dao.MySQLDataAccessObject;
import de.uni_potsdam.hpi.dao.PostgreSQLDataAccessObject;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.FileUtils;

public class SPIDERDatabase extends SPIDER implements InclusionDependencyAlgorithm, DatabaseConnectionParameterAlgorithm, StringParameterAlgorithm, BooleanParameterAlgorithm {

	public enum Database {
		MYSQL, DB2, POSTGRESQL, FILE
	}
	
	public enum Identifier {
		INPUT_GENERATOR, INPUT_ROW_LIMIT, DATABASE_NAME, DATABASE_TYPE, TABLE_NAMES, TEMP_FOLDER_PATH, CLEAN_TEMP
	};

	@Override
	public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement> configs = new ArrayList<ConfigurationRequirement>(7);
		configs.add(new ConfigurationRequirementDatabaseConnection(SPIDERDatabase.Identifier.INPUT_GENERATOR.name()));
		configs.add(new ConfigurationRequirementString(SPIDERDatabase.Identifier.INPUT_ROW_LIMIT.name()));
		configs.add(new ConfigurationRequirementString(SPIDERDatabase.Identifier.DATABASE_NAME.name()));
		configs.add(new ConfigurationRequirementString(SPIDERDatabase.Identifier.DATABASE_TYPE.name()));
		configs.add(new ConfigurationRequirementString(SPIDERDatabase.Identifier.TABLE_NAMES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
		configs.add(new ConfigurationRequirementString(SPIDERDatabase.Identifier.TEMP_FOLDER_PATH.name()));
		configs.add(new ConfigurationRequirementBoolean(SPIDERDatabase.Identifier.CLEAN_TEMP.name()));
		return configs;
	}

	@Override
	public void setDatabaseConnectionGeneratorConfigurationValue(String identifier, DatabaseConnectionGenerator... values) throws AlgorithmConfigurationException {
		if (SPIDERDatabase.Identifier.INPUT_GENERATOR.name().equals(identifier))
			this.databaseConnectionGenerator = values[0];
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setResultReceiver(InclusionDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
		if (SPIDERDatabase.Identifier.INPUT_ROW_LIMIT.name().equals(identifier))
			this.inputRowLimit = Integer.parseInt(values[0]);
		else if (SPIDERDatabase.Identifier.DATABASE_NAME.name().equals(identifier))
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
		else if (SPIDERDatabase.Identifier.TABLE_NAMES.name().equals(identifier))
			this.tableNames = values;
		else if (SPIDERDatabase.Identifier.TEMP_FOLDER_PATH.name().equals(identifier)) {
			if ("".equals(values[0]) || " ".equals(values[0]) || "/".equals(values[0]) || "\\".equals(values[0]) || File.separator.equals(values[0]) || FileUtils.isRoot(new File(values[0])))
				throw new AlgorithmConfigurationException(SPIDERDatabase.Identifier.TEMP_FOLDER_PATH + " must not be \"" + values[0] + "\"");
			this.tempFolderPath = values[0] + "SPIDER_temp" + File.separator;
		}
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	@Override
	public void setBooleanConfigurationValue(String identifier, boolean... values) throws AlgorithmConfigurationException {
		if (SPIDERDatabase.Identifier.CLEAN_TEMP.name().equals(identifier))
			this.cleanTemp = values[0];
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
