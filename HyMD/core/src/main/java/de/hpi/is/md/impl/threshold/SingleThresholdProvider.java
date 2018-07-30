package de.hpi.is.md.impl.threshold;

import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.IteratorUtils;
import de.hpi.is.md.util.OptionalDouble;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class SingleThresholdProvider {

	@NonNull
	private final DoubleSortedSet steps;

	static SingleThresholdProvider of(Iterable<Double> steps) {
		DoubleSortedSet finalSteps = StreamUtils.seq(steps)
			.filter(SingleThresholdProvider::isValid)
			.toSet(DoubleRBTreeSet::new);
		return new SingleThresholdProvider(finalSteps);
	}

	private static boolean isValid(double threshold) {
		return SimilarityMeasure.MIN_SIMILARITY < threshold
			&& threshold <= SimilarityMeasure.MAX_SIMILARITY;
	}

	DoubleSortedSet getAll() {
		return steps;
	}

	OptionalDouble getNext(double threshold) {
		DoubleIterator it = steps.iterator(threshold);
		return IteratorUtils.next(it);
	}
}
