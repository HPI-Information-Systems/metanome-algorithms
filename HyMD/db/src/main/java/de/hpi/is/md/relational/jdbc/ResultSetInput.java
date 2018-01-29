package de.hpi.is.md.relational.jdbc;

import de.hpi.is.md.relational.InputCloseException;
import de.hpi.is.md.relational.RelationalInput;
import de.hpi.is.md.relational.Row;
import de.hpi.is.md.relational.Schema;
import java.sql.ResultSet;
import java.util.Iterator;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ResultSetInput implements RelationalInput {

	@NonNull
	private final ResultSetIterator rows;

	static RelationalInput create(ResultSet resultSet) {
		ResultSetIterator rows = ResultSetIterator.create(resultSet);
		return new ResultSetInput(rows);
	}

	@Override
	public void close() throws InputCloseException {
		rows.close();
	}

	@Override
	public Schema getSchema() {
		return rows.getSchema();
	}

	@Override
	public Iterator<Row> iterator() {
		return rows;
	}
}
