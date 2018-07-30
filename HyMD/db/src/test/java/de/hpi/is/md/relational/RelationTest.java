package de.hpi.is.md.relational;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class RelationTest {

	@Mock
	private Relation relation;
	@Mock
	private RelationalInput input;
	@Mock
	private Schema schema;

	@Test
	public void test() throws InputOpenException {
		when(relation.open()).thenReturn(input);
		when(input.getSchema()).thenReturn(schema);
		when(relation.getSchema()).thenCallRealMethod();
		assertThat(relation.getSchema()).isEqualTo(schema);
	}

	@Test(expected = RuntimeException.class)
	public void testInputCloseException() throws InputException {
		when(relation.open()).thenReturn(input);
		when(input.getSchema()).thenReturn(schema);
		when(relation.getSchema()).thenCallRealMethod();
		doThrow(InputCloseException.class).when(input).close();
		relation.getSchema();
		fail();
	}

	@Test(expected = RuntimeException.class)
	public void testInputOpenException() throws InputOpenException {
		when(relation.open()).thenThrow(InputOpenException.class);
		when(relation.getSchema()).thenCallRealMethod();
		relation.getSchema();
		fail();
	}

}