package de.hpi.is.md.sim;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.UnorderedPair;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(CPSTypeIdResolver.class)
@CPSBase
public interface SimilarityMeasure<T> extends Serializable, Hashable {

	double MAX_SIMILARITY = 1.0;
	double MIN_SIMILARITY = 0.0;

	default SimilarityClassifier<T> asClassifier(double threshold) {
		return new DefaultSimilarityClassifier<>(threshold, this);
	}

	double calculateSimilarity(T obj1, T obj2);

	default double calculateSimilarity(UnorderedPair<T> pair) {
		return calculateSimilarity(pair.getFirst(), pair.getSecond());
	}

}
