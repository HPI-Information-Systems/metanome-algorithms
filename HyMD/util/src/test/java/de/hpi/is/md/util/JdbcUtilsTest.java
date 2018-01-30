package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class JdbcUtilsTest {

	@Mock
	private ResultSetMetaData metaData;

	@Test
	public void testGetColumnClass() throws SQLException {
		String notExistingClassName = "foo.bar.MyClass";
		doReturn(String.class.getName()).when(metaData).getColumnClassName(0);
		doReturn(notExistingClassName).when(metaData).getColumnClassName(1);
		assertThat(JdbcUtils.getColumnClass(metaData, 0)).hasValue(String.class);
		assertThat(JdbcUtils.getColumnClass(metaData, 1)).isEmpty();
	}

}
