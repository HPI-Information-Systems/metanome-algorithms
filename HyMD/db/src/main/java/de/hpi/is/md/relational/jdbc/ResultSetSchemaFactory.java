package de.hpi.is.md.relational.jdbc;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.Schema;
import de.hpi.is.md.util.JdbcUtils;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
final class ResultSetSchemaFactory {

	@NonNull
	private final ResultSetMetaData metaData;

	static Schema createSchema(ResultSetMetaData metaData) throws SQLException {
		return new ResultSetSchemaFactory(metaData).createSchema();
	}

	private Schema createSchema() throws SQLException {
		int columnCount = metaData.getColumnCount();
		Builder<Column<?>> builder = ImmutableList.builder();
		for (int i = 1; i <= columnCount; i++) {
			with(i).getColumn()
				.ifPresent(builder::add);
		}
		List<Column<?>> columns = builder.build();
		return Schema.of(columns);
	}

	private WithId with(int id) {
		return new WithId(id);
	}

	@RequiredArgsConstructor
	private class WithId {

		private final int id;

		private Column<?> createColumn(Class<?> type) throws SQLException {
			String columnName = metaData.getColumnName(id);
			String tableName = getTableName();
			return Column.of(columnName, type, tableName);
		}

		private Optional<Column<?>> getColumn() throws SQLException {
			return getColumnClass()
				.map(Unchecked.function(this::createColumn));
		}

		private Optional<Class<?>> getColumnClass() throws SQLException {
			Optional<Class<?>> columnClass = JdbcUtils.getColumnClass(metaData, id);
			if (!columnClass.isPresent()) {
				String className = metaData.getColumnClassName(id);
				log.warn("Class of column {} not found: {}", Integer.valueOf(id), className);
			}
			return columnClass;
		}

		private String getTableName() throws SQLException {
			String tableName = metaData.getTableName(id);
			return Strings.isNullOrEmpty(tableName) ? null : tableName;
		}
	}
}