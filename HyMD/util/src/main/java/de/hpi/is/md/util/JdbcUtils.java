package de.hpi.is.md.util;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JdbcUtils {

	public static Optional<Class<?>> getColumnClass(ResultSetMetaData metaData, int column)
		throws SQLException {
		String className = metaData.getColumnClassName(column);
		try {
			Class<?> clazz = Class.forName(className);
			return Optional.of(clazz);
		} catch (ClassNotFoundException e) {
			return Optional.empty();
		}
	}
}