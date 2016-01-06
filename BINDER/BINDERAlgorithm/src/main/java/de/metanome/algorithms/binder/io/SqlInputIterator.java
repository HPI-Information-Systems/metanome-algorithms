package de.metanome.algorithms.binder.io;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.uni_potsdam.hpi.dao.DataAccessObject;
import de.uni_potsdam.hpi.utils.DatabaseUtils;

public class SqlInputIterator implements InputIterator {
	
	private ResultSet resultSet = null;
	private List<String> record = null;
	
	public SqlInputIterator(DatabaseConnectionGenerator inputGenerator, DataAccessObject dao, String tableName, int inputRowLimit) throws InputGenerationException, AlgorithmConfigurationException {
		this.resultSet = inputGenerator.generateResultSetFromSql(dao.buildSelectEverythingQuery(tableName, inputRowLimit));
	}
	
	@Override
	public boolean next() throws InputIterationException {
		try {
			boolean hasNext = this.resultSet.next();
			
			if (hasNext) {
				int numColumns = this.resultSet.getMetaData().getColumnCount();
				this.record = new ArrayList<String>(numColumns);
				
				for (int columnIndex = 1; columnIndex <= numColumns; columnIndex++) {
					String value = this.resultSet.getString(columnIndex);
					
					// Replace line breaks with the zero-character, because these line breaks would otherwise split values when later written to plane-text buckets
					if (value != null)
						value = value.replaceAll("\n", "\0");
					this.record.add(value);
				}
			}
			
			return hasNext;
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new InputIterationException(e.getMessage());
		}
	}
	
	@Override
	public String getValue(int columnIndex) throws InputIterationException {
		return this.record.get(columnIndex);
	}

	@Override
	public List<String> getValues() throws InputIterationException {
		return this.record;
	}
	
	@Override
	public void close() throws Exception {
		Statement statement = this.resultSet.getStatement();
		DatabaseUtils.close(this.resultSet);
		DatabaseUtils.close(statement);
	}
}
