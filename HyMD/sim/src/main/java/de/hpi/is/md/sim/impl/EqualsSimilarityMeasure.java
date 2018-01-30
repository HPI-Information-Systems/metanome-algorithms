package de.hpi.is.md.sim.impl;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.jackson.SingletonDeserializer;
import java.util.Objects;

@JsonDeserialize(using = SingletonDeserializer.class)
@CPSType(id = "equals", base = SimilarityMeasure.class)
public enum EqualsSimilarityMeasure implements SimilarityMeasure<Object> {

	INSTANCE;

	@SuppressWarnings("unchecked")
	public static <T> SimilarityMeasure<T> getInstance() {
		return (SimilarityMeasure<T>) INSTANCE;
	}

	@Override
	public double calculateSimilarity(Object obj1, Object obj2) {
		return Objects.equals(obj1, obj2) ? MAX_SIMILARITY : MIN_SIMILARITY;
	}

	@Override
	public String toString() {
		return "Equal";
	}
}
