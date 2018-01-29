package de.hpi.is.md.relational;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.util.Hashable;
import java.io.Serializable;
import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "impl", defaultImpl = ColumnImpl.class)
@JsonTypeIdResolver(CPSTypeIdResolver.class)
@CPSBase
public interface Column<T> extends HasName, Serializable, Hashable {

	static <T> Column<T> of(String name, Class<T> type, String tableName) {
		return new ColumnImpl<>(name, type, tableName);
	}

	static <T> Column<T> of(String name, Class<T> type) {
		return of(name, type, null);
	}

	Optional<String> getTableName();

	Class<T> getType();

}
