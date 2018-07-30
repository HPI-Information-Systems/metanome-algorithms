package de.hpi.is.md.mapping.impl;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.mapping.SchemaMapper;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.relational.Schema;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CPSType(id = "self", base = SchemaMapper.class)
public class SelfSchemaMapper implements SchemaMapper {

	private static <T> ColumnPair<T> toPair(Column<T> column) {
		return new ColumnPair<>(column, column);
	}

	@Override
	public Collection<ColumnPair<?>> create(Schema schema) {
		List<Column<?>> columns = schema.getColumns();
		return columns.stream()
			.map(SelfSchemaMapper::toPair)
			.collect(Collectors.toList());
	}

	@Override
	public Collection<ColumnPair<?>> create(Schema schema1, Schema schema2) {
		return Collections.emptyList();
	}
}
