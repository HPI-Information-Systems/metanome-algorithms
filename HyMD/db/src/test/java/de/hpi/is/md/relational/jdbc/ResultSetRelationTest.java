package de.hpi.is.md.relational.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.InputCloseException;
import de.hpi.is.md.relational.InputException;
import de.hpi.is.md.relational.InputOpenException;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.relational.RelationalInput;
import de.hpi.is.md.relational.Row;
import de.hpi.is.md.util.JdbcTest;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ResultSetRelationTest extends JdbcTest {

	private static final Column<String> NAME = Column.of("NAME", String.class, "PERSON");

	@BeforeClass
	public static void createSchema() throws URISyntaxException, SQLException {
		URL url = ResultSetRelationTest.class.getResource("schema.sql");
		createSchema(url);
	}

	@Before
	public void importDataSet() throws Exception {
		try (InputStream in = ResultSetRelationTest.class.getResourceAsStream("dataset.xml")) {
			importDataSet(in);
		}
	}

	@Test
	public void testOpen() throws SQLException, InputCloseException, InputOpenException {
		try (Statement statement = connection.createStatement()) {
			String query = "SELECT * FROM Person";
			Relation relation = new ResultSetRelation(statement, query);
			try (RelationalInput input = relation.open()) {
				Row bob = input.iterator().next();
				assertThat(bob.get(NAME)).hasValue("Bob");
			}
			try (RelationalInput input = relation.open()) {
				Row bob = input.iterator().next();
				assertThat(bob.get(NAME)).hasValue("Bob");
			}
		}
	}

	@Test
	public void testSize() throws SQLException, InputException {
		try (Statement statement = connection.createStatement()) {
			String query = "SELECT * FROM Person";
			Relation relation = new ResultSetRelation(statement, query);
			assertThat(relation.getSize()).isEqualTo(3L);
		}
		try (Statement statement = connection.createStatement()) {
			String query = "SELECT * FROM PERSON WHERE name IS NULL";
			Relation relation = new ResultSetRelation(statement, query);
			assertThat(relation.getSize()).isEqualTo(0L);
		}
	}

	@Test(expected = InputOpenException.class)
	public void testOpenException() throws SQLException, InputOpenException, InputCloseException {
		Statement statement = Mockito.mock(Statement.class);
		String query = "SELECT * FROM Person";
		when(statement.executeQuery(query)).thenThrow(SQLException.class);
		Relation relation = new ResultSetRelation(statement, query);
		try (RelationalInput ignored = relation.open()) {
			fail();
		}
	}

}