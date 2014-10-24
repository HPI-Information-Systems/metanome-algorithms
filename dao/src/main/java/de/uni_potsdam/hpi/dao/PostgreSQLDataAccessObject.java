package de.uni_potsdam.hpi.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PostgreSQLDataAccessObject extends DataAccessObject {

	@Override
	public String getDriverClassName() {
		return "org.postgresql.Driver";
	}

	@Override
	public String limitSuffix(int limit) {
		if (limit <= 0)
			return "";
		return " LIMIT " + limit;
	}
	
	@Override
	public PreparedStatement insertValuesIntoStatement(PreparedStatement statement, String[] values, String[] valueTypes, int offset) throws NumberFormatException, SQLException {
		for (int i = 0; i < values.length; i++) {
			String valueType = valueTypes[i + offset].toLowerCase();
			
			if ((values[i] == null) || (values[i].equals("")))  {
				if (valueType.contains("long") || valueType.contains("big") || valueType.contains("int8"))
					statement.setNull(i + 1, java.sql.Types.BIGINT);
				else if (valueType.contains("int") || valueType.contains("serial"))
					statement.setNull(i + 1, java.sql.Types.INTEGER);
				else if (valueType.contains("float") || valueType.contains("numeric") || valueType.contains("decimal") || valueType.contains("real") || valueType.contains("precision"))
					statement.setNull(i + 1, java.sql.Types.FLOAT);
				else if (valueType.contains("bool"))
					statement.setNull(i + 1, java.sql.Types.BOOLEAN);
				else if (valueType.contains("date"))
					statement.setNull(i + 1, java.sql.Types.DATE);
				else 
					statement.setNull(i + 1, java.sql.Types.VARCHAR);
			}
			else {
				if (valueType.contains("long") || valueType.contains("big") || valueType.contains("int8"))
					statement.setLong(i + 1, Long.valueOf(values[i]).longValue());
				else if (valueType.contains("int") || valueType.contains("serial"))
					statement.setInt(i + 1, Integer.valueOf(values[i]).intValue());
				else if (valueType.contains("float") || valueType.contains("numeric") || valueType.contains("decimal") || valueType.contains("real") || valueType.contains("precision"))
					statement.setFloat(i + 1, Float.valueOf(values[i]).shortValue());
				else if (valueType.contains("bool"))
					statement.setBoolean(i + 1, Boolean.valueOf(values[i]).booleanValue());
				else if (valueType.contains("date"))
					statement.setDate(i + 1, Date.valueOf(values[i]));
				else 
					statement.setString(i + 1, values[i]);
			}
		}
		return statement;
	}

	@Override
	public String buildSelectDistinctSortedStringColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT " + attributeName + " COLLATE \"C\" AS " + attributeName + "_sorted " +
			   "FROM " + tableName + " " +
			   "ORDER BY " + attributeName + "_sorted";
	}

	@Override
	public String buildSelectDistinctSortedNumberColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT " + attributeName + ", CAST(" + attributeName + " AS VARCHAR(50)) COLLATE \"C\" AS " + attributeName + "_sorted " +
			   "FROM " + tableName + " " +
			   "ORDER BY " + attributeName + "_sorted";
	}

	@Override
	public String buildSelectDistinctSortedBinaryColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT CAST(" + attributeName + " AS VARCHAR(255)) COLLATE \"C\" AS " + attributeName + "_sorted " +
			   "FROM " + tableName + " " +
			   "ORDER BY " + attributeName + "_sorted";
	}
	
	@Override
	public String buildCreateIndexQuery(String tableName, String attributeName) {
		return "CREATE INDEX index_" + tableName + "_" + attributeName + " ON " + tableName + " (" + attributeName + ")";
	}
	
	@Override
	public String buildDropIndexQuery(String tableName, String attributeName) {
		return "DROP INDEX index_" + tableName + "_" + attributeName;
	}

	@Override
	public String buildColumnMetaQuery(String databaseName, String tableName) {
		return "SELECT DISTINCT COLUMN_NAME, UDT_NAME, CHARACTER_MAXIMUM_LENGTH, ORDINAL_POSITION " +
			   "FROM INFORMATION_SCHEMA.COLUMNS " +
			   "WHERE LOWER(TABLE_NAME) = LOWER('" + tableName + "') " +
			   "AND LOWER(TABLE_CATALOG) = LOWER('" + databaseName + "') " +
			   "ORDER BY ORDINAL_POSITION ASC";
	}

	@Override
	public String buildTableQuery(String databaseName) {
		return "SELECT DISTINCT TABLE_NAME " +
			   "FROM INFORMATION_SCHEMA.COLUMNS " +
			   "WHERE TABLE_SCHEMA = 'public' " +
			   "AND TABLE_CATALOG = '" + databaseName + "'";
	}
	
	@Override
	public void extract(List<String> names, List<String> types, List<String> basicTypes, ResultSet columnsResultSet) throws SQLException {
		while (columnsResultSet.next()) {
			names.add(columnsResultSet.getString("COLUMN_NAME"));
			
			String type = columnsResultSet.getString("UDT_NAME");
			String typeLength = columnsResultSet.getString("CHARACTER_MAXIMUM_LENGTH");
			
			if ((typeLength == null) || (typeLength.equals("")))
				types.add(type);
			else
				types.add(type + "(" + typeLength + ")");
			basicTypes.add(type);
		}
	}
}
