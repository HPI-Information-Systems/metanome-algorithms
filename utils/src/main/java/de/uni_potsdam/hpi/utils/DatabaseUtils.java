package de.uni_potsdam.hpi.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtils {

	public static void close(ResultSet resultSet) {
		try {
			if (resultSet != null)
				resultSet.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void close(Statement statement) {
		try {
			if (statement != null)
				statement.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void close(Connection connection) {
		try {
			if (connection != null) {
				if (!connection.getAutoCommit())
					connection.commit();
				connection.close();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static String[] generateAttributeNames(int numAttributes) {
		String prefix = "A";
		String[] names = new String[numAttributes];
		
		for (int i = 0; i < numAttributes; i++)
			names[i] = prefix + (i + 1);
		
		return names;
	}

	public static String[] generateAttributeTypes(int numAttributes) {
		String[] types = new String[numAttributes];
		
		for (int i = 0; i < numAttributes; i++)
			types[i] = "VARCHAR(255)";
		
		return types;
	}

	// Format the attribute labels so that they can be used as labels in a table
	public static void formatAttributeLabels(String[] firstLine) {
		for (int i = 0; i < firstLine.length; i++)
			firstLine[i] = firstLine[i].replace("-", "_").replace("/", "_");
	}

	public static boolean matchSameDataTypeClass(String dataType1, String dataType2) {
		if (dataType1.equals(dataType2))
			return true;
		if (isNumeric(dataType1) && isNumeric(dataType2))
			return true;
		if (isString(dataType1) && isString(dataType2))
			return true;
		if (isTemporal(dataType1) && isTemporal(dataType2))
			return true;
		return false;
	}
	
	public static boolean isNumeric(String dataType) {
		if (dataType == null)
			return false;
		String type = dataType.toLowerCase();
		if (type.contains("int") || 
			type.contains("float") || 
			type.contains("double") || 
			type.contains("numeric") || 
			type.contains("decimal") || 
			type.contains("real") || 
			type.contains("precision") || 
			type.contains("serial") || 
			type.contains("bit"))
			return true;
		return false;
	}
	
	public static boolean isString(String dataType) {
		if (dataType == null || DatabaseUtils.isLargeObject(dataType))
			return false;
		String type = dataType.toLowerCase();
		if (type.contains("char") || 
			type.contains("text"))
			return true;
		return false;
	}
	
	public static boolean isTemporal(String dataType) {
		if (dataType == null)
			return false;
		String type = dataType.toLowerCase();
		if (type.contains("date") || 
			type.contains("time") || 
			type.contains("year"))
			return true;
		return false;
	}
	
	public static boolean isLargeObject(String dataType) {
		if (dataType == null)
			return false;
		String type = dataType.toLowerCase();
		if (type.contains("object") ||
			type.contains("clob") ||
			type.contains("blob"))
			return true;
		return false;
	}
	
	public static boolean isIndexable(String dataType) {
		if ((dataType == null) || dataType.equals(""))
			return false;
		
		// If we cannot measure the size of the data type or the size is very large, the attribute should not be indexed
		String size = dataType.replaceAll("\\D", "");
		if (size.equals("") || Integer.parseInt(size) > 200)
			return false;
		
		return true;
	}
	
}
