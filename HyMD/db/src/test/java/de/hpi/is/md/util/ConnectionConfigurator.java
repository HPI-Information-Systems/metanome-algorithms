package de.hpi.is.md.util;

import java.util.HashMap;
import java.util.Map;
import org.dbunit.DefaultOperationListener;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.datatype.IDataTypeFactory;

public class ConnectionConfigurator extends DefaultOperationListener {

	private final Map<String, Object> properties = new HashMap<>();

	@Override
	public void connectionRetrieved(IDatabaseConnection connection) {
		super.connectionRetrieved(connection);
		applyProperties(connection);
	}

	public ConnectionConfigurator setDataTypeFactory(IDataTypeFactory dataTypeFactory) {
		return setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataTypeFactory);
	}

	public ConnectionConfigurator setProperty(String name, Object value) {
		properties.put(name, value);
		return this;
	}

	private void applyProperties(IDatabaseConnection connection) {
		DatabaseConfig dbConfig = connection.getConfig();
		properties.forEach(dbConfig::setProperty);
	}
}
