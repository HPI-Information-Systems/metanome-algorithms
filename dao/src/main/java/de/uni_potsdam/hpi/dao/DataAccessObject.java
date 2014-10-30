package de.uni_potsdam.hpi.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.DatabaseUtils;

public abstract class DataAccessObject {

	public String buildDropTableQuery(String tableName) {
		return "DROP TABLE " + tableName;
	}

	public String buildCreateTableQuery(String tableName, String[] attributeNames, String[] types) {
		return "CREATE TABLE " + tableName + " (" + CollectionUtils.concat(attributeNames, types, " ", ",") + ")";
	}

	public String buildInsertQuery(String tableName, int numAttributes) {
		return "INSERT INTO " + tableName + " VALUES (" + CollectionUtils.concat(numAttributes, "?", ",") + ")";
	}

	public String buildDeleteQuery(String tableName, String[] attributeNames) {
		return "DELETE FROM " + tableName + " WHERE " + CollectionUtils.concat(attributeNames, "", " = ? ", "AND "); // TODO: delete a row only once if there are multiple duplicate rows; for mysql: DELETE TOP 1 FROM db.tablename WHERE {condition for ex id = 5};
	}

	public String buildSelectColumnQuery(String tableName, String attributeName) {
		return "SELECT " + attributeName + " " +
			   "FROM " + tableName;
	}

	public String buildSelectColumnQuery(String tableName, String attributeName, int limit) {
		return this.buildSelectColumnQuery(tableName, attributeName) + this.limitSuffix(limit);
	}

	public String buildSelectDistinctColumnQuery(String tableName, String attributeName) {
		return this.buildSelectColumnQuery(tableName, "DISTINCT " + attributeName);
	}

	public String buildSelectDistinctColumnQuery(String tableName, String attributeName, int limit) {
		return this.buildSelectColumnQuery(tableName, "DISTINCT " + attributeName, limit);
	}

	public String buildSelectEverythingQuery(String tableName) {
		return this.buildSelectColumnQuery(tableName, "*");
	}

	public String buildSelectEverythingQuery(String tableName, int limit) { // Do not use limit in the select * query if you are using cursors: With limit, the DBMS materializes the result of this call (=copy the limit-lines) and does not use the cursor directly on the relation
		return this.buildSelectColumnQuery(tableName, "*", limit);
	}

	public String buildSelectDistinctSortedColumnQuery(String tableName, String attributeName, String attributeType) {
		if (DatabaseUtils.isString(attributeType))
			return this.buildSelectDistinctSortedStringColumnQuery(tableName, attributeName);
		else if (DatabaseUtils.isNumeric(attributeType) || DatabaseUtils.isTemporal(attributeType))
			return this.buildSelectDistinctSortedNumberColumnQuery(tableName, attributeName);
		else
			return this.buildSelectDistinctSortedBinaryColumnQuery(tableName, attributeName);
	}

	public String buildSelectDistinctSortedColumnQuery(String tableName, String attributeName, String attributeType, int limit) {
		if (limit <= 0)
			return this.buildSelectDistinctSortedColumnQuery(tableName, attributeName, attributeType);
		String subquery = "(" + this.buildSelectColumnQuery(tableName, attributeName, limit) + ") AS " + tableName + "_subset";
		return this.buildSelectDistinctSortedColumnQuery(subquery, attributeName, attributeType);
	}

	public String buildCountRowsQuery(String tableName) {
		return "SELECT COUNT(*) " +
			   "FROM " + tableName;
	}

	public String buildSelectValueQuery(String tableName, String attributeName, String value) {
		return "SELECT " + attributeName + " " +
			   "FROM " + tableName + " " +
			   "WHERE " + attributeName + " = '" + value + "' ";
	}
	
	public String buildSelectValueQuery(String tableName, String attributeName, String value, int limit) {
		return this.buildSelectValueQuery(tableName, attributeName, value) + this.limitSuffix(limit);
	}
	
	public String buildSelectValuesQuery(String tableName, String attributeName, Set<String> values) {
		StringBuilder builder = new StringBuilder(
			   "SELECT DISTINCT " + attributeName + " " +
			   "FROM " + tableName + " " +
			   "WHERE ");
		
		int valuesRemaining = values.size();
		for (String value : values) {
			builder.append(attributeName + " = '" + value + "' ");
			valuesRemaining--;
			if (valuesRemaining > 0)
				builder.append("OR ");
		}
		
		return builder.toString();
	}
	
	public String buildSelectValuesQuery(String tableName, String attributeName, Set<String> values, int limit) {
		return this.buildSelectValuesQuery(tableName, attributeName, values) + this.limitSuffix(limit);
	}
	
	// This SQL-statement does not work if columns have different types!!!	
	public String buildSelectColumnExceptColumnQuery(String fromTableName, String fromAttributeName, String exceptTableName, String exceptAttributeName, int limit) {
		return "SELECT DISTINCT fromTable." + fromAttributeName + " " +
			   "FROM " + fromTableName + " fromTable LEFT OUTER JOIN " + exceptTableName + " exceptTable " +
			   "ON fromTable." + fromAttributeName + " = exceptTable." + exceptAttributeName + " " +
			   "WHERE exceptTable." + exceptAttributeName + " IS NULL " +
			   "AND fromTable." + fromAttributeName + " IS NOT NULL " + 
			   this.limitSuffix(limit);
	}

	// This SQL-statement does not work if columns have different types!!!	
	public String buildSelectColumnCombinationNotInColumnCombinationQuery(String fromTableName, String[] fromAttributeNames, String exceptTableName, String[] exceptAttributeNames, int limit) {
		String selectAttributes = CollectionUtils.concat(fromAttributeNames, "fromTable.", "", ", ");
		StringBuilder joinAttributes = new StringBuilder();
		for (int i = 0; i < fromAttributeNames.length; i++) {
			joinAttributes.append("fromTable." + fromAttributeNames[i] + " = exceptTable." + exceptAttributeNames[i] + " ");
			if (i + 1 < fromAttributeNames.length)
				joinAttributes.append("AND ");
		}
		
		String nullAttributes = CollectionUtils.concat(exceptAttributeNames, "exceptTable.", " IS NULL ", "AND ");
		String notNullAttributes = CollectionUtils.concat(fromAttributeNames, "fromTable.", " IS NOT NULL ", "OR ");
		
		return "SELECT DISTINCT " + selectAttributes + " " +
			   "FROM " + fromTableName + " fromTable LEFT OUTER JOIN " + exceptTableName + " exceptTable " +
			   "ON " + joinAttributes.toString() +
			   "WHERE " + nullAttributes +
			   "AND ( " + notNullAttributes + ") " +
			   this.limitSuffix(limit);
	}
	
/*	public String buildSelectColumnExceptColumnQuery(String fromTableName, String fromAttributeName, String exceptTableName, String exceptAttributeName, int limit) {
		return "SELECT DISTINCT " + fromAttributeName + " " +
			   "FROM " + fromTableName + " " +
			   "WHERE " + fromAttributeName + " NOT IN ( SELECT " + exceptAttributeName + " FROM " + exceptTableName + " ) " +
			   this.limitSuffix(limit);
	}
*/	
	public abstract String getDriverClassName();
	
	public abstract String limitSuffix(int limit);
	
	public abstract PreparedStatement insertValuesIntoStatement(PreparedStatement statement, String[] values, String[] valueTypes, int offset) throws NumberFormatException, SQLException;

	public abstract String buildSelectDistinctSortedStringColumnQuery(String tableName, String attributeName);

	public abstract String buildSelectDistinctSortedNumberColumnQuery(String tableName, String attributeName);

	public abstract String buildSelectDistinctSortedBinaryColumnQuery(String tableName, String attributeName);
	
	public abstract String buildCreateIndexQuery(String tableName, String attributeName);
	
	public abstract String buildDropIndexQuery(String tableName, String attributeName);
	
	public abstract String buildColumnMetaQuery(String databaseName, String tableName);

	public abstract String buildTableQuery(String databaseName);
	
	public abstract void extract(List<String> names, List<String> types, List<String> basicTypes, ResultSet columnsResultSet) throws SQLException;

	@Override
	public int hashCode() {
		return this.getDriverClassName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.getDriverClassName().equals(obj);
	}

	@Override
	public String toString() {
		return this.getDriverClassName();
	}

	
}
