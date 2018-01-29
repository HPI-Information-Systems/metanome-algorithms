package de.hpi.is.md.mapping.impl;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Multimap;
import de.hpi.is.md.mapping.SchemaMapper;
import de.hpi.is.md.mapping.SchemaMapperHelper;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.relational.Schema;
import de.hpi.is.md.util.Optionals;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@CPSType(id = "fixed", base = SchemaMapper.class)
public class FixedSchemaMapper implements SchemaMapper {

	@NonNull
	@JsonDeserialize(converter = ColumnMappingsConverter.class)
	private final Multimap<Column<?>, Column<?>> pairs;

	@Override
	public Collection<ColumnPair<?>> create(Schema schema1, Schema schema2) {
		List<Column<?>> columns = schema1.getColumns();
		return columns.stream()
			.flatMap(this::getMatching)
			.collect(Collectors.toList());
	}

	private <T> Stream<ColumnPair<T>> getMatching(Column<T> column) {
		Collection<Column<?>> columns = pairs.get(column);
		return columns.stream()
			.map(otherColumn -> SchemaMapperHelper.toPair(column, otherColumn))
			.flatMap(Optionals::stream);
	}
}
