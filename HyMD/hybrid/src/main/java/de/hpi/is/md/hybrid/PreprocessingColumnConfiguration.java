package de.hpi.is.md.hybrid;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Dictionary;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
public class PreprocessingColumnConfiguration<T> implements Hashable {

	@NonNull
	private final SimilarityComputer<T> similarityComputer;
	@Getter
	@NonNull
	private final ColumnMapping<T> mapping;
	private final double minThreshold;
	@NonNull
	private SimilarityIndexBuilder indexBuilder;

	public SimilarityIndex createIndex(Dictionary<T> leftDictionary, Dictionary<T> rightDictionary,
		PositionListIndex rightIndex) {
		SimilarityMeasure<T> similarityMeasure = mapping.getSimilarityMeasure();
		return indexBuilder
			.create(leftDictionary, rightDictionary, similarityMeasure, similarityComputer,
				minThreshold, rightIndex);
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.put(mapping)
			.put(indexBuilder)
			.put(similarityComputer)
			.putDouble(minThreshold);
	}

	@Override
	public String toString() {
		return '{' +
			"mapping=" + mapping +
			", similarityComputer=" + similarityComputer +
			", minThreshold=" + minThreshold +
			'}';
	}
}
