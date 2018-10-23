package de.hpi.is.md.relational.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.google.common.collect.Iterables;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.InputCloseException;
import de.hpi.is.md.relational.RelationalInput;
import de.hpi.is.md.relational.Row;
import de.hpi.is.md.relational.Schema;
import de.hpi.is.md.util.JdbcTest;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResultSetInputTest extends JdbcTest {

	private static final Column<String> NAME = Column.of("NAME", String.class, "PERSON");
	private static final Column<String> LAST_NAME = Column.of("LAST_NAME", String.class, "PERSON");
	private static final Column<Short> AGE = Column.of("AGE", Short.class, "PERSON");

	@BeforeClass
	public static void createSchema() throws URISyntaxException, SQLException {
		URL url = ResultSetInputTest.class.getResource("schema.sql");
		createSchema(url);
	}

	@Before
	public void importDataSet() throws Exception {
		try (InputStream in = ResultSetInputTest.class.getResourceAsStream("dataset.xml")) {
			importDataSet(in);
		}
	}

	@Test
	public void testBob() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			try (RelationalInput input = ResultSetInput.create(resultSet)) {
				Iterator<Row> rows = input.iterator();
				assertThat(rows.hasNext()).isTrue();
				Row bob = rows.next();
				assertThat(bob.get(NAME)).hasValue("Bob");
				assertThat(bob.get(LAST_NAME)).isEmpty();
				assertThat(bob.get(AGE)).hasValue(Short.valueOf((short) 18));
			}
		}
	}

	@Test(expected = RuntimeException.class)
	public void testClosedResultSetAfterNext() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			try (RelationalInput input = ResultSetInput.create(resultSet)) {
				Iterator<Row> it = input.iterator();
				assertThat(it.hasNext()).isTrue();
				resultSet.close();
				it.next();
				fail();
			}
		}
	}

	@Test(expected = InputCloseException.class)
	public void testClosedResultSetBeforeClose() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			ResultSet spy = spy(resultSet);
			doThrow(new SQLException()).when(spy).close();
			try (RelationalInput ignored = ResultSetInput.create(spy)) {
			}
		}
	}

	@Test(expected = RuntimeException.class)
	public void testClosedResultSetBeforeCreation() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			resultSet.close();
			try (RelationalInput ignored = ResultSetInput.create(resultSet)) {
				fail();
			}
		}
	}

	@Test(expected = RuntimeException.class)
	public void testClosedResultSetBeforeNext() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			try (RelationalInput input = ResultSetInput.create(resultSet)) {
				resultSet.close();
				for (@SuppressWarnings("unused") Row ignored : input) {
					fail();
				}
			}
		}
	}

	@Test
	public void testMultipleHasNext() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			try (RelationalInput input = ResultSetInput.create(resultSet)) {
				Iterator<Row> rows = input.iterator();
				assertThat(rows.hasNext()).isTrue();
				assertThat(rows.hasNext()).isTrue();
				Row bob = rows.next();
				assertThat(bob.get(NAME)).hasValue("Bob");
			}
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void testNoSuchElement() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			try (RelationalInput input = ResultSetInput.create(resultSet)) {
				Iterator<Row> it = input.iterator();
				for (int i = 0; i < 4; i++) {
					it.next();
				}
				fail();
			}
		}
	}

	@Test
	public void testNumberOfRows() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			try (RelationalInput input = ResultSetInput.create(resultSet)) {
				assertThat(Iterables.size(input)).isEqualTo(3);
			}
		}
	}

	@Test
	public void testSchema() throws SQLException, InputCloseException {
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM Person");
			try (RelationalInput input = ResultSetInput.create(resultSet)) {
				Schema schema = input.getSchema();
				List<Column<?>> columns = schema.getColumns();
				assertThat(columns.get(0).getType()).isEqualTo(Integer.class);
				assertThat(columns.get(0).getName()).isEqualToIgnoringCase("ID");
				assertThat(columns.get(1).getType()).isEqualTo(String.class);
				assertThat(columns.get(1).getName()).isEqualToIgnoringCase("NAME");
				assertThat(columns.get(2).getType()).isEqualTo(String.class);
				assertThat(columns.get(2).getName()).isEqualToIgnoringCase("LAST_NAME");
				assertThat(columns.get(3).getType()).isEqualTo(Short.class);
				assertThat(columns.get(3).getName()).isEqualToIgnoringCase("AGE");
				assertThat(schema.getColumn("ID")).isPresent();
				assertThat(schema.getColumn("nAmE")).isPresent();
				assertThat(schema.getColumn("LAST_NAME")).isPresent();
				assertThat(schema.getColumn("AGE")).isPresent();
				assertThat(schema.getColumn("FIRST_NAME")).isEmpty();
				for (Row row : input) {
					assertThat(row.getSchema()).isEqualTo(schema);
				}
			}
		}
	}


}
