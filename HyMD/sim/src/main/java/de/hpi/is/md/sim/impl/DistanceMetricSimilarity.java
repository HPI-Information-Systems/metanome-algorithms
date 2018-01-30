package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.sim.DistanceMetric;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.ObjectUtils;
import lombok.RequiredArgsConstructor;

@CPSType(id = "distance", base = SimilarityMeasure.class)
@RequiredArgsConstructor
public class DistanceMetricSimilarity<T> implements SimilarityMeasure<T> {

	private static final long serialVersionUID = 7031625598851432099L;
	private final DistanceMetric<T> metric;
	private final int max;

	@Override
	public double calculateSimilarity(T obj1, T obj2) {
		if (ObjectUtils.bothNull(obj1, obj2)) {
			return MAX_SIMILARITY;
		}
		if (ObjectUtils.eitherNull(obj1, obj2)) {
			return MIN_SIMILARITY;
		}
		long distance = metric.computeDistance(obj1, obj2);
		return normalize(distance);
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putClass(DistanceMetricSimilarity.class)
			.put(metric)
			.putInt(max);
	}

	@Override
	public String toString() {
		return "Uniform" + metric;
	}

	private double normalize(long distance) {
		double result = normalizeUnchecked(distance);
		assert MIN_SIMILARITY <= result && result <= MAX_SIMILARITY
			: "Similarity must be in interval [0.0, 1.0]";
		return result;
	}

	private double normalizeNotEmpty(long distance) {
		double inverseDistance = Math.max((max - distance), 0.0);
		return inverseDistance / max;
	}

	private double normalizeUnchecked(long distance) {
		return max == 0 ? MAX_SIMILARITY : normalizeNotEmpty(distance);
	}
}
