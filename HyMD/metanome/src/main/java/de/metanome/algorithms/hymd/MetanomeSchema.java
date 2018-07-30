package de.metanome.algorithms.hymd;

import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.Schema;
import de.hpi.is.md.util.StreamUtils;
import java.util.List;
import java.util.stream.Collectors;

class MetanomeSchema {

	static Schema getSchema(de.metanome.algorithm_integration.input.RelationalInput input) {
		List<String> columnNames = input.columnNames();
		String tableName = input.relationName();
		return with(tableName).of(columnNames);
	}

	private static WithTableName with(String tableName) {
		return new WithTableName(tableName);
	}

	private static class WithTableName {

		private final String tableName;

		private WithTableName(String tableName) {
			this.tableName = tableName;
		}

		private List<Column<?>> getColumns(Iterable<String> columnNames) {
			return StreamUtils.seq(columnNames)
				.map(this::toColumn)
				.collect(Collectors.toList());
		}

		private Schema of(Iterable<String> columnNames) {
			List<Column<?>> columns = getColumns(columnNames);
			return Schema.of(columns);
		}

		private Column<String> toColumn(String columnName) {
			return Column.of(columnName, String.class, tableName);
		}

	}
}
