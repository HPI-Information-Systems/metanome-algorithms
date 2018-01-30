package de.hpi.is.md.relational;

import java.util.Map;
import java.util.Optional;

public interface Row extends HasSchema {

	static Row create(Schema schema, Map<Column<?>, Object> values) {
		return RowImpl.create(schema, values);
	}

	<T> Optional<T> get(Column<T> column);
}
