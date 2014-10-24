package de.uni_potsdam.hpi.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DB2DataAccessObject extends DataAccessObject {

	@Override
	public String getDriverClassName() {
		return "com.ibm.db2.jcc.DB2Driver";
	}

	@Override
	public String limitSuffix(int limit) {
		if (limit <= 0)
			return "";
		return " FETCH FIRST " + limit + " ROWS ONLY";
	}
	
	@Override
	public PreparedStatement insertValuesIntoStatement(PreparedStatement statement, String[] values, String[] valueTypes, int offset) throws NumberFormatException, SQLException {
		for (int i = 0; i < values.length; i++)
			statement.setString(i + 1, values[i]);
		return statement;
	}
	
	@Override
	public String buildSelectDistinctSortedStringColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT " + attributeName + " " +
			   "FROM " + tableName + " " +
			   "ORDER BY " + attributeName + " "; // TODO: The collation works somehow like this: select * from test order by COLLATION_KEY_BIT(str,'UCA400R1_AS_LSV_S2')
	}

	@Override
	public String buildSelectDistinctSortedNumberColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT " + attributeName + ", CAST(" + attributeName + " AS VARCHAR(50)) " +
			   "FROM " + tableName + " " +
			   "ORDER BY CAST(" + attributeName + " AS VARCHAR(50))";
	}

	@Override
	public String buildSelectDistinctSortedBinaryColumnQuery(String tableName, String attributeName) {
		return "SELECT DISTINCT CAST(" + attributeName + " AS VARCHAR(255)) " +
			   "FROM " + tableName + " " +
			   "ORDER BY CAST(" + attributeName + " AS VARCHAR(255))";
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
		return "SELECT DISTINCT COLUMN_NAME, ORDINAL_POSITION, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH " +
			   "FROM SYSIBM.COLUMNS " +
			   "WHERE LOWER(TABLE_NAME) = LOWER('" + tableName + "') " +
			   "ORDER BY ORDINAL_POSITION ASC";
	}

	@Override
	public String buildTableQuery(String databaseName) {
		return "SELECT DISTINCT TABLE_NAME " +
			   "FROM SYSIBM.COLUMNS " + 
			   "WHERE TABLE_SCHEMA = '" + databaseName + "' ";
	}

	@Override
	public void extract(List<String> names, List<String> types, List<String> basicTypes, ResultSet columnsResultSet) throws SQLException {
		while (columnsResultSet.next()) {
			names.add(columnsResultSet.getString("COLUMN_NAME"));
			
			String basicType = columnsResultSet.getString("DATA_TYPE");
			if (basicType.equals("CHARACTER VARYING"))
				types.add("VARCHAR(" + columnsResultSet.getInt("CHARACTER_MAXIMUM_LENGTH") + ")");
			else
				types.add(basicType);
			basicTypes.add(basicType);
		}
	}

}
