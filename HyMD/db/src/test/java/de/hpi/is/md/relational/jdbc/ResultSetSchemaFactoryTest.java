package de.hpi.is.md.relational.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.Schema;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ResultSetSchemaFactoryTest {

	@Mock
	private ResultSetMetaData metaData;

	@Test
	public void test() throws SQLException {
		when(metaData.getColumnCount()).thenReturn(2);
		doReturn(String.class.getName()).when(metaData).getColumnClassName(1);
		doReturn(Integer.class.getName()).when(metaData).getColumnClassName(2);
		doReturn("a").when(metaData).getColumnName(1);
		doReturn("b").when(metaData).getColumnName(2);
		Schema schema = ResultSetSchemaFactory.createSchema(metaData);
		assertThat(schema.getColumns()).hasSize(2);
		assertThat(schema.getColumns()).contains(Column.of("a", String.class));
		assertThat(schema.getColumns()).contains(Column.of("b", Integer.class));
	}

	@Test
	public void testClassNotFound() throws SQLException {
		when(metaData.getColumnCount()).thenReturn(1);
		when(metaData.getColumnClassName(1)).thenReturn("foo.bar.Baz");
		Schema schema = ResultSetSchemaFactory.createSchema(metaData);
		assertThat(schema.getColumns()).isEmpty();
	}

	@Test
	public void testTableName() throws SQLException {
		when(metaData.getColumnCount()).thenReturn(1);
		when(metaData.getColumnClassName(1)).thenReturn(String.class.getName());
		when(metaData.getColumnName(1)).thenReturn("a");
		when(metaData.getTableName(1)).thenReturn("t");
		Schema schema = ResultSetSchemaFactory.createSchema(metaData);
		assertThat(schema.getColumns()).hasSize(1);
		assertThat(schema.getColumns()).contains(Column.of("a", String.class, "t"));
	}

}