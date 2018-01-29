package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.BigDecimalUtils;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.ObjectUtils;
import de.hpi.is.md.util.jackson.EnumNameDeserializer;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;

@JsonDeserialize(using = EnumNameDeserializer.class)
@CPSType(id = "levenshtein", base = SimilarityMeasure.class)
@RequiredArgsConstructor
public enum LevenshteinSimilarity implements SimilarityMeasure<String> {

	EXACT(ExactLevenshteinComputer.INSTANCE), FAST(FastLevenshteinComputer.INSTANCE);

	private static final LevenshteinDistance LEVENSHTEIN = LevenshteinDistance.getDefaultInstance();
	private final LevenshteinComputer computer;

	@Override
	public double calculateSimilarity(String obj1, String obj2) {
		if (ObjectUtils.bothNull(obj1, obj2)) {
			return MAX_SIMILARITY;
		}
		if (ObjectUtils.eitherNull(obj1, obj2)) {
			return MIN_SIMILARITY;
		}
		int distance = LEVENSHTEIN.apply(obj1, obj2);
		int length1 = obj1.length();
		int length2 = obj2.length();
		int maxLength = Math.max(length1, length2);
		return normalize(distance, maxLength);
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putClass(LevenshteinSimilarity.class)
			.put(computer);
	}

	@Override
	public String toString() {
		return "Levenshtein";
	}

	private double normalize(int distance, int maxLength) {
		double result = normalizeUnchecked(distance, maxLength);
		assert MIN_SIMILARITY <= result && result <= MAX_SIMILARITY
			: "Similarity must be in interval [0.0, 1.0]";
		return result;
	}

	private double normalizeUnchecked(int distance, int maxLength) {
		return maxLength == 0 ? MAX_SIMILARITY : computer.normalizeNotEmpty(distance, maxLength);
	}

	private enum FastLevenshteinComputer implements LevenshteinComputer {

		INSTANCE;

		@Override
		public double normalizeNotEmpty(int distance, int maxLength) {
			return (maxLength - distance) / (double) maxLength;
		}
	}

	private enum ExactLevenshteinComputer implements LevenshteinComputer {

		INSTANCE;

		private static final MathContext CONTEXT = new MathContext(10);

		private static BigDecimal normalizeNotEmpty(BigDecimal distance, BigDecimal maxLength) {
			return maxLength.subtract(distance).divide(maxLength, CONTEXT);
		}

		@Override
		public double normalizeNotEmpty(int distance, int maxLength) {
			BigDecimal decimalDistance = BigDecimalUtils.valueOf(distance);
			BigDecimal decimalLength = BigDecimalUtils.valueOf(maxLength);
			BigDecimal normalized = normalizeNotEmpty(decimalDistance, decimalLength);
			return normalized.doubleValue();
		}
	}

	private interface LevenshteinComputer extends Serializable, Hashable {

		double normalizeNotEmpty(int distance, int maxLength);
	}
}
