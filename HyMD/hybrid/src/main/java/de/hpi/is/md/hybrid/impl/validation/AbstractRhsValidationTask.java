package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.HyMDProperties;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Int2ObjectHashMultimap;
import de.hpi.is.md.util.Int2ObjectMultimap;
import de.hpi.is.md.util.IntArrayPair;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.IntIterable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class AbstractRhsValidationTask implements RhsValidationTask {

	@NonNull
	private final PreprocessedColumnPair columnPair;
	@NonNull
	private final DictionaryRecords rightRecords;
	private final Collection<IntArrayPair> violations = new ArrayList<>();
	private final int rhsAttr;
	@NonNull
	private final Predicate<Collection<IntArrayPair>> shouldUpdate;
	@NonNull
	private final Classifier classifier;
	private final double from;
	private final double lhsSimilarity;
	private double threshold = SimilarityMeasure.MAX_SIMILARITY;

	@Override
	public RhsResult createResult() {
		return RhsResult.builder()
			.rhsAttr(rhsAttr)
			.from(from)
			.threshold(threshold)
			.violations(violations)
			.validAndMinimal(isValidAndMinimal())
			.build();
	}

	@Override
	public boolean shouldUpdate() {
		return shouldUpdate(threshold);
	}

	@Override
	public void validate(Iterable<int[]> leftMatches, IntIterable right) {
		Int2ObjectMultimap<int[]> grouped = groupByValue(leftMatches);
		Iterator<Entry<Collection<int[]>>> it = grouped.iterator();
		while (it.hasNext() && shouldUpdate()) {
			Entry<Collection<int[]>> entry = it.next();
			int value = entry.getIntKey();
			Collection<int[]> records = entry.getValue();
			threshold = updateThreshold(value, records, right);
		}
	}

	@Override
	public void validate(int[] record, IntIterable right) {
		if (shouldUpdate()) {
			int leftValue = getLeftValue(record);
			Collection<int[]> records = Collections.singletonList(record);
			threshold = updateThreshold(leftValue, records, right);
		}
	}

	protected boolean isValidAndMinimal(double similarity) {
		return classifier.isValidAndMinimal(similarity);
	}

	protected boolean shouldUpdate(double similarity) {
		return isValidAndMinimal(similarity) || shouldUpdateViolations();
	}

	protected abstract WithRecords with(int value, Collection<int[]> records);

	int[] getRightRecord(int id) {
		return rightRecords.get(id);
	}

	boolean isTrivial(double similarity) {
		return similarity <= lhsSimilarity;
	}

	private void addViolation(IntArrayPair violation) {
		violations.add(violation);
	}

	private double calculateMinSimilarity(int value, Collection<int[]> records,
		IntIterable matching) {
		return with(value, records).calculateMinSimilarity(matching);
	}

	private int getLeftValue(int[] left) {
		return columnPair.getLeftValue(left);
	}

	private Int2ObjectMultimap<int[]> groupByValue(Iterable<int[]> leftMatches) {
		Int2ObjectMultimap<int[]> grouped = new Int2ObjectHashMultimap<>();
		for (int[] record : leftMatches) {
			int leftValue = getLeftValue(record);
			grouped.put(leftValue, record);
		}
		return grouped;
	}

	private boolean isValidAndMinimal() {
		return isValidAndMinimal(threshold);
	}

	private boolean shouldUpdateViolations() {
		return HyMDProperties.isSamplingEnabled() && shouldUpdate.test(violations);
	}

	private double updateThreshold(int value, Collection<int[]> records, IntIterable matching) {
		double minSimilarity = calculateMinSimilarity(value, records, matching);
		return Math.min(threshold, minSimilarity);
	}

	@RequiredArgsConstructor
	protected abstract class WithRecords {

		@NonNull
		private final Collection<int[]> left;
		private final int leftValue;

		protected double getSimilarity(int[] right) {
			int rightValue = getRightValue(right);
			return columnPair.getSimilarity(leftValue, rightValue);
		}

		void addViolation(int[] right) {
			Collection<IntArrayPair> violations = asViolation(right);
			violations.forEach(AbstractRhsValidationTask.this::addViolation);
		}

		abstract double calculateMinSimilarity(IntIterable matching);

		int getRightValue(int[] right) {
			return columnPair.getRightValue(right);
		}

		double getSimilarity(int rightValue) {
			return columnPair.getSimilarity(leftValue, rightValue);
		}

		private Collection<IntArrayPair> asViolation(int[] right) {
			return StreamUtils.seq(left)
				.map(l -> new IntArrayPair(l, right))
				.toList();
		}
	}
}
