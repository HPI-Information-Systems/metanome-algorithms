package de.hpi.is.md.relational;

import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
class SchemaImpl implements Schema {

	@NonNull
	private final List<Column<?>> columns;

}
