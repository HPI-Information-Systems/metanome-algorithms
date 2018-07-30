package de.hpi.is.md.hybrid.impl.preprocessed;

import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.Row;
import de.hpi.is.md.util.DefaultDictionary;
import de.hpi.is.md.util.Dictionary;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ColumnCompressor<T> {

	private final Dictionary<T> dictionary = new DefaultDictionary<>();
	private final PositionListIndex.Builder pliBuilder = ArrayPositionListIndex.builder();
	@NonNull
	private final Column<T> column;

	int add(Row row, int recordId) {
		int valueId = addToDictionary(row);
		addToPli(recordId, valueId);
		return valueId;
	}

	CompressedColumn<T> build() {
		PositionListIndex pli = pliBuilder.build();
		return new CompressedColumn<>(dictionary, pli);
	}

	private int addToDictionary(Row row) {
		T value = row.get(column)
			.orElse(null);
		return dictionary.getOrAdd(value);
	}

	private void addToPli(int recordId, int value) {
		pliBuilder.add(recordId, value);
	}
}
