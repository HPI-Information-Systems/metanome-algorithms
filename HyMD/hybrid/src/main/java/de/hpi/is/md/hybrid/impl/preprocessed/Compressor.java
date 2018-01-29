package de.hpi.is.md.hybrid.impl.preprocessed;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.Row;
import io.astefanutti.metrics.aspectj.Metrics;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Metrics
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class Compressor {

	@NonNull
	private final Collection<ColumnCompressor<?>> columnCompressors;
	@NonNull
	private final Map<Column<?>, Integer> columnIndexes;
	@NonNull
	private final DictionaryRecords.Builder records;
	private int recordId = 0;

	static CompressorBuilder builder() {
		return new CompressorBuilder();
	}

	CompressedRelation compress(Iterable<Row> input) {
		input.forEach(this::add);
		return toRelation();
	}

	private void add(Row row) {
		with(row).add();
	}

	private List<CompressedColumn<?>> buildColumns() {
		return columnCompressors.stream()
			.map(ColumnCompressor::build)
			.collect(Collectors.toList());
	}

	private DictionaryRecords buildDictionaryRecords() {
		return records.build();
	}

	private CompressedRelation toRelation() {
		DictionaryRecords dictionaryRecords = buildDictionaryRecords();
		List<CompressedColumn<?>> columns = buildColumns();
		return new CompressedRelation(columns, dictionaryRecords, columnIndexes);
	}

	private WithRow with(Row row) {
		return new WithRow(row);
	}

	@RequiredArgsConstructor
	private class WithRow {

		@NonNull
		private final Row row;

		private void add() {
			int[] record = addToColumns();
			records.add(recordId++, record);
		}

		private int addTo(ColumnCompressor<?> column) {
			return column.add(row, recordId);
		}

		private int[] addToColumns() {
			return columnCompressors.stream()
				.mapToInt(this::addTo)
				.toArray();
		}
	}

}
