package de.metanome.algorithms.binder.io;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.uni_potsdam.hpi.dao.DataAccessObject;
import de.uni_potsdam.hpi.utils.DatabaseUtils;

public class SqlInputIterator implements InputIterator {
	
	private ResultSet resultSet = null;
	
	public SqlInputIterator(DatabaseConnectionGenerator inputGenerator, DataAccessObject dao, String tableName, int inputRowLimit) throws InputGenerationException {
		this.resultSet = inputGenerator.generateResultSetFromSql(dao.buildSelectEverythingQuery(tableName, inputRowLimit));
	}
	
	@Override
	public boolean next() throws InputIterationException {
		try {
			return this.resultSet.next();
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new InputIterationException(e.getMessage());
		}
	}
	
	@Override
	public String getValue(int columnIndex) throws InputIterationException {
		try {
			return this.resultSet.getString(columnIndex + 1);
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new InputIterationException(e.getMessage());
		}
	}

	@Override
	public List<String> getValues(int numColumns) throws InputIterationException {
		List<String> values = new ArrayList<String>(numColumns);
		for (int columnIndex = 0; columnIndex < numColumns; columnIndex++)
			values.add(this.getValue(columnIndex));
		return values;
	}
	
	@Override
	public void close() throws Exception {
		Statement statement = this.resultSet.getStatement();
		DatabaseUtils.close(this.resultSet);
		DatabaseUtils.close(statement);
	}
}
