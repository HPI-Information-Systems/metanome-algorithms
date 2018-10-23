package de.hpi.is.md.relational.jdbc;

import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.HasSchema;
import de.hpi.is.md.relational.InputCloseException;
import de.hpi.is.md.relational.Row;
import de.hpi.is.md.relational.Schema;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ResultSetIterator implements Iterator<Row>, AutoCloseable, HasSchema {

	@NonNull
	private final ResultSet resultSet;
	@Getter
	@NonNull
	private final Schema schema;
	private Boolean currentNext = null;

	static ResultSetIterator create(ResultSet resultSet) {
		Schema schema = createSchema(resultSet);
		return new ResultSetIterator(resultSet, schema);
	}

	private static Schema createSchema(ResultSet resultSet) {
		try {
			ResultSetMetaData metaData = resultSet.getMetaData();
			return ResultSetSchemaFactory.createSchema(metaData);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws InputCloseException {
		try {
			if (!resultSet.isClosed()) {
				resultSet.close();
			}
		} catch (SQLException e) {
			throw new InputCloseException("Error closing result set", e);
		}
	}

	@Override
	public boolean hasNext() {
		return getNext().orElseGet(this::moveCursor).booleanValue();
	}

	@Override
	public Row next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		Row row = getCurrentRow();
		clearNext();
		return row;
	}

	private void clearNext() {
		currentNext = null;
	}

	private Row getCurrentRow() {
		Map<Column<?>, Object> values = getCurrentValues();
		return Row.create(schema, values);
	}

	private <T> T getCurrentValue(Column<T> column) {
		try {
			String columnName = column.getName();
			Class<T> type = column.getType();
			return resultSet.getObject(columnName, type);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<Column<?>, Object> getCurrentValues() {
		Collection<Column<?>> columns = schema.getColumns();
//		this is not null safe
//		return StreamUtils.seq(columns)
//			.map(this::toEntry)
//			.toMap(Tuple2::v1, Tuple2::v2);
		Map<Column<?>, Object> map = new HashMap<>();
		for (Column<?> column : columns) {
			Object value = getCurrentValue(column);
			map.put(column, value);
		}
		return map;
	}

	private Optional<Boolean> getNext() {
		return Optional.ofNullable(currentNext);
	}

	private boolean moveCursor() {
		try {
			currentNext = Boolean.valueOf(resultSet.next());
			return currentNext.booleanValue();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
