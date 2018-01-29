package de.hpi.is.md.hybrid.impl.preprocessed;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.InputException;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.relational.RelationalInput;
import de.hpi.is.md.relational.Schema;
import java.util.List;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Setter
class CompressorBuilder {

	@NonNull
	private DictionaryRecords.Builder records = MapDictionaryRecords.builder();

	@Timed
	CompressedRelation compress(Relation relation) {
		try (RelationalInput input = relation.open()) {
			return compress(input);
		} catch (InputException e) {
			throw new RuntimeException(e);
		}
	}

	private CompressedRelation compress(RelationalInput input) {
		Schema schema = input.getSchema();
		Compressor compressor = create(schema);
		return compressor.compress(input);
	}

	private Compressor create(Schema schema) {
		List<Column<?>> columns = schema.getColumns();
		ImmutableCollection.Builder<ColumnCompressor<?>> columnCompressors = ImmutableList
			.builder();
		ImmutableMap.Builder<Column<?>, Integer> columnIndexes = ImmutableMap.builder();
		int columnIndex = 0;
		for (Column<?> column : columns) {
			ColumnCompressor<?> compressor = new ColumnCompressor<>(column);
			columnCompressors.add(compressor);
			columnIndexes.put(column, columnIndex++);
		}
		return new Compressor(columnCompressors.build(), columnIndexes.build(), records);
	}

}
