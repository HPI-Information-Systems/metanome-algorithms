package de.hpi.is.md.relational.jdbc;

import de.hpi.is.md.relational.InputException;
import de.hpi.is.md.relational.InputOpenException;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.relational.RelationalInput;
import de.hpi.is.md.relational.Schema;
import de.hpi.is.md.util.Hasher;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResultSetRelation implements Relation {

	@NonNull
	private final Statement statement;
	@NonNull
	private final String query;

	@Override
	public Schema getSchema() {
		try (RelationalInput input = open("SELECT * FROM (" + query + ") rel LIMIT 0")) {
			return input.getSchema();
		} catch (InputException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getSize() throws InputException {
		try (ResultSet resultSet = executeQuery("SELECT COUNT(*) FROM (" + query + ") rel")) {
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
			throw new IllegalStateException("No count returned");
		} catch (SQLException e) {
			throw new InputOpenException(e);
		}
	}

	@Override
	public RelationalInput open() throws InputOpenException {
		return open(query);
	}

	@Override
	public void hash(Hasher hasher) {
		hasher.putUnencodedChars(query);
	}

	private ResultSet executeQuery(String sql) throws SQLException {
		return statement.executeQuery(sql);
	}

	private RelationalInput open(String sql) throws InputOpenException {
		try {
			ResultSet resultSet = executeQuery(sql);
			return ResultSetInput.create(resultSet);
		} catch (SQLException e) {
			throw new InputOpenException(e);
		}
	}
}
