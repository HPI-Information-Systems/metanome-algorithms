package de.hpi.is.md.hybrid.impl.preprocessed;

import com.codahale.metrics.Timer.Context;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.hybrid.ArrayDictionaryRecords;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.Preprocessed;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.PreprocessingConfiguration;
import de.hpi.is.md.hybrid.Preprocessor;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.util.Dictionary;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.MetricsUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;

@Builder
public class PreprocessorImpl implements Preprocessor {

	private static final HashFunction HASH_FUNCTION = Hashing.murmur3_128();
	@NonNull
	private final PreprocessingConfiguration mappings;
	@NonNull
	private final Relation left;
	@NonNull
	private final Relation right;
	@NonNull
	@Default
	private DictionaryRecords.Builder leftBuilder = ArrayDictionaryRecords.builder();
	@NonNull
	@Default
	private DictionaryRecords.Builder rightBuilder = ArrayDictionaryRecords.builder();

	private static List<Dictionary<?>> getDictionaries(CompressedRelation compressed) {
		int size = compressed.size();
		List<Dictionary<?>> dictionaries = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			CompressedColumn<Object> column = compressed.getColumn(i);
			dictionaries.add(column.getDictionary());
		}
		return dictionaries;
	}

	@Override
	public Preprocessed get() {
		try (Context ignored = MetricsUtils.timer(PreprocessorImpl.class, "get")) {
			return get_();
		}
	}

	@Override
	public HashCode hash() {
		return Hasher.of(HASH_FUNCTION)
			.put(left)
			.put(right)
			.put(mappings)
			.put(leftBuilder)
			.put(rightBuilder)
			.hash();
	}

	private Preprocessed build(List<PreprocessedColumnPair> columnPairs,
		DictionaryRecords leftRecords, DictionaryRecords rightRecords,
		List<Dictionary<?>> leftDictionaries,
		List<Dictionary<?>> rightDictionaries) {
		List<ColumnMapping<?>> columnMappings = mappings.getColumnMappings();
		return Preprocessed.builder()
			.columnPairs(columnPairs)
			.leftRecords(leftRecords)
			.mappings(columnMappings)
			.rightRecords(rightRecords)
			.leftDictionaries(leftDictionaries)
			.rightDictionaries(rightDictionaries)
			.build();
	}

	private List<PreprocessedColumnPair> createColumnPairs(CompressedRelation compressedLeft,
		CompressedRelation compressedRight) {
		return PreprocessedColumnPairImpl.builder()
			.left(compressedLeft)
			.right(compressedRight)
			.mappings(mappings)
			.build();
	}

	private Preprocessed get_() {
		CompressedRelation compressedLeft = Compressor.builder()
			.records(leftBuilder)
			.compress(left);
		CompressedRelation compressedRight = Compressor.builder()
			.records(rightBuilder)
			.compress(right);
		List<PreprocessedColumnPair> columnPairs = createColumnPairs(compressedLeft,
			compressedRight);
		DictionaryRecords leftRecords = compressedLeft.getDictionaryRecords();
		DictionaryRecords rightRecords = compressedRight.getDictionaryRecords();
		return build(columnPairs, leftRecords, rightRecords, getDictionaries(compressedLeft),
			getDictionaries(compressedRight));
	}

}
