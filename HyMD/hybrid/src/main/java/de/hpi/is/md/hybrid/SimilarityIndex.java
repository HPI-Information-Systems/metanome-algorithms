package de.hpi.is.md.hybrid;

import com.bakdata.util.jackson.CPSBase;
import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Dictionary;
import de.hpi.is.md.util.Hashable;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.io.Serializable;

public interface SimilarityIndex extends Serializable {

	double getMinSimilarity();

	IntCollection getSimilarRecords(int valueId, double threshold);

	DoubleSet getSimilarities();

	double getSimilarity(int leftValue, int rightValue);

	@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonTypeIdResolver(CPSTypeIdResolver.class)
	@CPSBase
	interface SimilarityIndexBuilder extends Hashable {

		<T> SimilarityIndex create(Dictionary<T> leftDictionary, Dictionary<T> rightDictionary,
			SimilarityMeasure<T> similarityMeasure, SimilarityComputer<T> similarityComputer,
			double minThreshold, PositionListIndex rightIndex);
	}
}
