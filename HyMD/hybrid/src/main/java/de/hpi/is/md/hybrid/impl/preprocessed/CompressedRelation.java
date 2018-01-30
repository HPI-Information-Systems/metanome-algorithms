package de.hpi.is.md.hybrid.impl.preprocessed;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.relational.Column;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class CompressedRelation {

	@NonNull
	private final List<CompressedColumn<?>> columns;
	@Getter
	@NonNull
	private final DictionaryRecords dictionaryRecords;
	@NonNull
	private final Map<Column<?>, Integer> columnIndexes;

	@SuppressWarnings("unchecked")
	<T> CompressedColumn<T> getColumn(int columnId) {
		CompressedColumn<?> column = columns.get(columnId);
		return (CompressedColumn<T>) column;
	}

	<T> int indexOf(Column<T> column) {
		return columnIndexes.get(column);
	}

	int size() {
		return columns.size();
	}

}
