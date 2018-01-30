package de.hpi.is.md.util;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;

public abstract class JdbcTest {

	private static final String JDBC_DRIVER = org.h2.Driver.class.getName();
	private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
	private static final String USER = "sa";
	private static final String PASSWORD = "";
	protected Connection connection;

	protected static void createSchema(URL url) throws URISyntaxException, SQLException {
		URI uri = url.toURI();
		createSchema(uri);
	}

	protected static void createSchema(URI uri) throws SQLException {
		File file = new File(uri);
		createSchema(file);
	}

	protected static void createSchema(File file) throws SQLException {
		String path = file.getAbsolutePath();
		RunScript.execute(JDBC_URL, USER, PASSWORD, path, Charset.defaultCharset(), false);
	}

	protected static void importDataSet(InputStream in) throws Exception {
		IDataSet dataSet = readDataSet(in);
		cleanlyInsert(dataSet);
	}

	private static void cleanlyInsert(IDataSet dataSet) throws Exception {
		IDatabaseTester databaseTester = new JdbcDatabaseTester(JDBC_DRIVER, JDBC_URL, USER,
			PASSWORD);
		databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
		databaseTester.setDataSet(dataSet);
		ConnectionConfigurator operationListener = new ConnectionConfigurator()
			.setDataTypeFactory(new H2DataTypeFactory());
		databaseTester.setOperationListener(operationListener);
		databaseTester.onSetup();
	}

	private static DataSource getDataSource() {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL(JDBC_URL);
		dataSource.setUser(USER);
		dataSource.setPassword(PASSWORD);
		return dataSource;
	}

	private static IDataSet readDataSet(InputStream in) throws DataSetException {
		return new FlatXmlDataSetBuilder().setColumnSensing(true).build(in);
	}

	@After
	public void closeConnection() throws SQLException {
		connection.close();
	}

	@Before
	public void openConnection() throws SQLException, DatabaseUnitException {
		connection = getDataSource().getConnection();
	}

}
