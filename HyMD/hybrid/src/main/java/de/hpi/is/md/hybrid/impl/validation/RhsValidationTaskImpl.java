package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTaskImplFactory.RhsValidationTaskImplFactoryBuilder;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.IntArrayPair;
import it.unimi.dsi.fastutil.ints.IntIterable;
import java.util.Collection;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Predicate;
import lombok.Builder;

public final class RhsValidationTaskImpl extends AbstractRhsValidationTask {

	@Builder
	private RhsValidationTaskImpl(PreprocessedColumnPair columnPair,
		DictionaryRecords rightRecords, int rhsAttr,
		Predicate<Collection<IntArrayPair>> shouldUpdate,
		Classifier classifier, double from, double lhsSimilarity) {
		super(columnPair, rightRecords, rhsAttr, shouldUpdate, classifier, from,
			lhsSimilarity);
	}

	public static RhsValidationTaskImplFactoryBuilder factoryBuilder() {
		return RhsValidationTaskImplFactory.builder();
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
			OfInt it = matching.iterator();
			//does order have an impact on runtime?
			while (it.hasNext() && shouldUpdate(minSimilarity)) {
				int id = it.nextInt();
				updateMinSimilarity(id);
			}
			return minSimilarity;
		}

		private void checkViolation(int[] right, double similarity) {
			if (!isValidAndMinimal(similarity) || isTrivial(similarity)) {
				addViolation(right);
			}
		}

		private void updateMinSimilarity(int[] right) {
			double similarity = getSimilarity(right);
			checkViolation(right, similarity);
			minSimilarity = Math.min(minSimilarity, similarity);
		}

		private void updateMinSimilarity(int id) {
			int[] right = getRightRecord(id);
			updateMinSimilarity(right);
		}
	}
}
