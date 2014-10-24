package de.uni_potsdam.hpi.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MySQLDataAccessObject extends DataAccessObject {

	@Override
	public String getDriverClassName() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public String limitSuffix(int limit) {
		if (limit <= 0)
			return "";
		return " LIMIT " + limit;
	}
	
	@Override
	public PreparedStatement insertValuesIntoStatement(PreparedStatement statement, String[] values, String[] valueTypes, int offset) throws NumberFormatException, SQLException {
		for (int i = 0; i < values.length; i++)
			statement.setString(i + 1, values[i]);
		return statement;
	}

	@Override
	public String buildSelectDistinctSortedStringColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT " + attributeName + " COLLATE utf8_bin AS " + attributeName + "_sorted " +
			   "FROM " + tableName + " " +
			   "ORDER BY " + attributeName + "_sorted";
	}

	@Override
	public String buildSelectDistinctSortedNumberColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT " + attributeName + ", CAST(" + attributeName + " AS VARCHAR(50)) COLLATE utf8_bin AS " + attributeName + "_sorted " +
			   "FROM " + tableName + " " +
			   "ORDER BY " + attributeName + "_sorted";
	}

	@Override
	public String buildSelectDistinctSortedBinaryColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT CAST(" + attributeName + " AS VARCHAR(255)) COLLATE utf8_bin AS " + attributeName + "_sorted " +
			   "FROM " + tableName + " " +
			   "ORDER BY " + attributeName + "_sorted";
	}
	
	@Override
	public String buildCreateIndexQuery(String tableName, String attributeName) {
		return "CREATE INDEX index_" + tableName + "_" + attributeName + " ON " + tableName + " (" + attributeName + ")";
	}
	
	@Override
	public String buildDropIndexQuery(String tableName, String attributeName) {
		return "DROP INDEX index_" + tableName + "_" + attributeName + " ON " + tableName;
	}

	@Override
	public String buildColumnMetaQuery(String databaseName, String tableName) {
		return "SELECT DISTINCT COLUMN_NAME, COLUMN_TYPE, ORDINAL_POSITION " +
			   "FROM INFORMATION_SCHEMA.COLUMNS " +
			   "WHERE LOWER(TABLE_NAME) = LOWER('" + tableName + "') " +
			   "AND LOWER(TABLE_SCHEMA) = LOWER('" + databaseName + "') " +
			   "ORDER BY ORDINAL_POSITION ASC";
	}

	@Override
	public String buildTableQuery(String databaseName) {
		return "SELECT DISTINCT TABLE_NAME " +
			   "FROM INFORMATION_SCHEMA.COLUMNS " +
			   "WHERE TABLE_SCHEMA = '" + databaseName + "'";
	}

	@Override
	public void extract(List<String> names, List<String> types, List<String> basicTypes, ResultSet columnsResultSet) throws SQLException {
		while (columnsResultSet.next()) {
			names.add(columnsResultSet.getString("COLUMN_NAME"));
			
			String type = columnsResultSet.getString("COLUMN_TYPE");
			types.add(type);
			basicTypes.add(type.replaceAll("\\d|\\(|\\)", ""));
		}
	}
}
