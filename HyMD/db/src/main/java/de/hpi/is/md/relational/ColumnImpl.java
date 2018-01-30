package de.hpi.is.md.relational;

import de.hpi.is.md.util.Hasher;
import java.util.Optional;
import lombok.Data;
import lombok.NonNull;

@Data
class ColumnImpl<T> implements Column<T> {

	private static final long serialVersionUID = -5020039258783289415L;
	@NonNull
	private final String name;
	@NonNull
	private final Class<T> type;
	private final String tableName;

	private static String asPrefix(String tableName) {
		return tableName.concat(".");
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putUnencodedChars(name)
			.putUnencodedChars(tableName)
			.putClass(type);
	}

	@Override
	public String toString() {
		return getTableName()
			.map(ColumnImpl::asPrefix)
			.orElse("") + name;
	}

	@Override
	public Optional<String> getTableName() {
		return Optional.ofNullable(tableName);
	}
}
