package de.hpi.is.md.mapping.impl;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.mapping.SchemaMapper;
import de.hpi.is.md.mapping.SchemaMapperHelper;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.relational.Schema;
import de.hpi.is.md.util.CollectionUtils;
import de.hpi.is.md.util.Optionals;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@CPSType(id = "type", base = SchemaMapper.class)
public class TypeSchemaMapper implements SchemaMapper {

	private static Collection<ColumnPair<?>> create(Iterable<Column<?>> columns1,
		Iterable<Column<?>> columns2) {
		return StreamUtils.seq(columns1).crossJoin(columns2)
			.map(t -> t.map(SchemaMapperHelper::toPair))
			.flatMap(Optionals::stream)
			.collect(Collectors.toList());
	}

	private static Collection<ColumnPair<?>> create(List<Column<?>> columns) {
		return StreamUtils.seq(CollectionUtils.crossProduct(columns))
			.map(t -> t.map(SchemaMapperHelper::toPair))
			.flatMap(Optionals::stream)
			.collect(Collectors.toList());
	}

	@Override
	public Collection<ColumnPair<?>> create(Schema schema) {
		List<Column<?>> columns = schema.getColumns();
		return create(columns);
	}

	@Override
	public Collection<ColumnPair<?>> create(Schema schema1, Schema schema2) {
		List<Column<?>> columns1 = schema1.getColumns();
		List<Column<?>> columns2 = schema2.getColumns();
		return create(columns1, columns2);
	}
}
