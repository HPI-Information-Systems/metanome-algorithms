package de.hpi.is.md.relational;

import de.hpi.is.md.util.CastUtils;
import java.util.List;
import java.util.Optional;

public interface Schema {

	static Schema of(List<Column<?>> columns) {
		return new SchemaImpl(columns);
	}

	default <T> Optional<Column<T>> getColumn(String columnName) {
		return getColumns().stream()
			.filter(column -> column.hasName(columnName))
			.findFirst()
			.map(CastUtils::as);
	}

	List<Column<?>> getColumns();
}
