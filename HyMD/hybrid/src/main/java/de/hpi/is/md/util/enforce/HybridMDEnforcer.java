package de.hpi.is.md.util.enforce;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.Preprocessed;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.util.Dictionary;
import de.hpi.is.md.util.DictionaryInverter;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HybridMDEnforcer implements MDEnforcer {

	private final EnforcerFactory factory;
	private final RecordInflater leftInflater;
	private final RecordInflater rightInflater;

	public static MDEnforcer create(Preprocessed preprocessed) {
		Builder builder = new Builder(preprocessed);
		return builder.build();
	}

	@Override
	public Collection<EnforceMatch> enforce(Collection<ColumnMatchWithThreshold<?>> lhs) {
		ActualEnforcer enforcer = factory.createEnforcer(lhs);
		Iterable<CompressedEnforceMatch> compressedEnforceMatches = enforcer.enforce();
		return inflate(compressedEnforceMatches);
	}

	private Collection<EnforceMatch> inflate(Iterable<CompressedEnforceMatch> compressed) {
		return StreamUtils.seq(compressed)
			.map(this::inflate)
			.toList();
	}

	private EnforceMatch inflate(CompressedEnforceMatch compressed) {
		Iterable<int[]> compressedLeft = compressed.getLeft();
		Iterable<Object[]> left = StreamUtils.seq(compressedLeft)
			.map(leftInflater::inflate)
			.toList();
		Iterable<int[]> compressedRight = compressed.getRight();
		Iterable<Object[]> right = StreamUtils.seq(compressedRight)
			.map(rightInflater::inflate)
			.toList();
		return new EnforceMatch(left, right);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class Builder {

		private final Preprocessed preprocessed;

		private static List<Int2ObjectMap<?>> invert(Iterable<Dictionary<?>> dictionaries) {
			return StreamUtils.seq(dictionaries)
				.map(DictionaryInverter::invert)
				.collect(Collectors.toList());
		}

		private MDEnforcer build() {
			DictionaryRecords leftRecords = preprocessed.getLeftRecords();
			DictionaryRecords rightRecords = preprocessed.getRightRecords();
			Map<ColumnMapping<?>, PreprocessedColumnPair> columnPairs = getColumnPairs();
			EnforcerFactory factory = EnforcerFactory.builder()
				.leftRecords(leftRecords)
				.rightRecords(rightRecords)
				.columnPairs(columnPairs)
				.build();
			List<Dictionary<?>> leftDictionaries = preprocessed.getLeftDictionaries();
			List<Int2ObjectMap<?>> leftInvertedDictionaries = invert(leftDictionaries);
			RecordInflater leftInflater = new RecordInflater(leftInvertedDictionaries);
			List<Dictionary<?>> rightDictionaries = preprocessed.getRightDictionaries();
			List<Int2ObjectMap<?>> rightInvertedDictionaries = invert(rightDictionaries);
			RecordInflater rightInflater = new RecordInflater(rightInvertedDictionaries);
			return new HybridMDEnforcer(factory, leftInflater, rightInflater);
		}

		private Map<ColumnMapping<?>, PreprocessedColumnPair> getColumnPairs() {
			Map<ColumnMapping<?>, PreprocessedColumnPair> columnPairs = new HashMap<>();
			List<ColumnMapping<?>> mappings = preprocessed.getMappings();
			List<PreprocessedColumnPair> preprocessedColumns = preprocessed.getColumnPairs();
			int i = 0;
			for (ColumnMapping<?> mapping : mappings) {
				PreprocessedColumnPair columnPair = preprocessedColumns.get(i);
				columnPairs.put(mapping, columnPair);
				i++;
			}
			return columnPairs;
		}

	}

}
