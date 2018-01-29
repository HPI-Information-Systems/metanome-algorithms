package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.impl.validation.GroupingRhsValidationTaskFactory.GroupingRhsValidationTaskFactoryBuilder;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Int2ObjectHashMultimap;
import de.hpi.is.md.util.Int2ObjectMultimap;
import de.hpi.is.md.util.IntArrayPair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.IntIterable;
import java.util.Collection;
import java.util.Iterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Predicate;
import lombok.Builder;

public final class GroupingRhsValidationTask extends AbstractRhsValidationTask {

	@Builder
	private GroupingRhsValidationTask(PreprocessedColumnPair columnPair,
		DictionaryRecords rightRecords, int rhsAttr,
		Predicate<Collection<IntArrayPair>> shouldUpdate,
		Classifier classifier, double from, double lhsSimilarity) {
		super(columnPair, rightRecords, rhsAttr, shouldUpdate, classifier, from,
			lhsSimilarity);
	}

	public static GroupingRhsValidationTaskFactoryBuilder factoryBuilder() {
		return GroupingRhsValidationTaskFactory.builder();
	}

	@Override
	protected WithRecords with(int value, Collection<int[]> records) {
		return new WithRecordsImpl(records, value);
	}

	private final class WithRecordsImpl extends WithRecords {

		private double minSimilarity = SimilarityMeasure.MAX_SIMILARITY;

		private WithRecordsImpl(Collection<int[]> left, int leftValue) {
			super(left, leftValue);
		}

		@Override
		public double calculateMinSimilarity(IntIterable matching) {
			Int2ObjectMultimap<int[]> map = groupByValue(matching);
			return calculateMinSimilarity(map);
		}

		private void addViolations(Iterable<int[]> records) {
			records.forEach(this::addViolation);
		}

		private double calculateMinSimilarity(Iterable<Entry<Collection<int[]>>> map) {
			Iterator<Entry<Collection<int[]>>> it = map.iterator();
			//does order have an impact on runtime?
			while (it.hasNext() && shouldUpdate(minSimilarity)) {
				Entry<Collection<int[]>> entry = it.next();
				int rightValue = entry.getIntKey();
				Collection<int[]> records = entry.getValue();
				updateMinSimilarity(rightValue, records);
			}
			return minSimilarity;
		}

		private void checkViolation(Iterable<int[]> records, double similarity) {
			if (!isValidAndMinimal(similarity) || isTrivial(similarity)) {
				addViolations(records);
			}
		}

		@SuppressWarnings("TypeMayBeWeakened")
		private Int2ObjectMultimap<int[]> groupByValue(IntIterable matching) {
			Int2ObjectMultimap<int[]> map = new Int2ObjectHashMultimap<>();
			OfInt it = matching.iterator();
			while (it.hasNext()) {
				int id = it.nextInt();
				int[] right = getRightRecord(id);
				int rightValue = getRightValue(right);
				map.put(rightValue, right);
			}
			return map;
		}

		private void updateMinSimilarity(int rightValue, Iterable<int[]> records) {
			double similarity = getSimilarity(rightValue);
			checkViolation(records, similarity);
			minSimilarity = Math.min(minSimilarity, similarity);
		}
	}
}
