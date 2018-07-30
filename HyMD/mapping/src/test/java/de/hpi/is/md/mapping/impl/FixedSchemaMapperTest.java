package de.hpi.is.md.mapping.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.hpi.is.md.mapping.SchemaMapper;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.relational.Schema;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FixedSchemaMapperTest {

	private static final Column<Integer> A = Column.of("a", Integer.class);
	private static final Column<Integer> B = Column.of("b", Integer.class);
	private static final Column<Integer> C = Column.of("c", Integer.class);
	@Mock
	private Relation relation;

	@Test
	public void test() {
		Multimap<Column<?>, Column<?>> map = ImmutableMultimap.<Column<?>, Column<?>>builder()
			.put(A, B)
			.put(A, C)
			.put(B, A)
			.build();
		SchemaMapper schemaMapper = new FixedSchemaMapper(map);
		List<Column<?>> columns = Arrays.asList(A, B, C);
		Schema schema = Schema.of(columns);
		when(relation.getSchema()).thenReturn(schema);
		Collection<ColumnPair<?>> mapping = schemaMapper.create(relation);
		assertThat(mapping).hasSize(3);
		assertThat(mapping).contains(new ColumnPair<>(A, B));
		assertThat(mapping).contains(new ColumnPair<>(A, C));
		assertThat(mapping).contains(new ColumnPair<>(B, A));
	}

}