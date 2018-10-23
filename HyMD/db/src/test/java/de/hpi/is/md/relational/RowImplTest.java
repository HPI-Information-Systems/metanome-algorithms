package de.hpi.is.md.relational;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class RowImplTest {

	private static final Column<String> A = Column.of("a", String.class);
	private static final Column<Integer> B = Column.of("b", Integer.class);
	private static final Column<Object> C = Column.of("c", Object.class);

	private static List<Column<?>> createColumns() {
		return Arrays.asList(A, B);
	}

	private static Row createRow() {
		Schema schema = createSchema();
		Map<Column<?>, Object> values = ImmutableMap.of(A, "foo", B, Integer.valueOf(0));
		return RowImpl.create(schema, values);
	}

	private static Schema createSchema() {
		List<Column<?>> columns = createColumns();
		return Schema.of(columns);
	}

	@Test
	public void testGet() {
		Row row = createRow();
		Optional<String> a = row.get(A);
		assertThat(a).hasValue("foo");
		Optional<Integer> b = row.get(B);
		assertThat(b).hasValue(Integer.valueOf(0));
		assertThat(row.get(C)).isEmpty();
	}

	@Test
	public void testWrongType() {
		Row row = createRow();
		Column<String> wrongB = Column.of("b", String.class);
		assertThat(row.get(wrongB)).isEmpty();
	}

}
