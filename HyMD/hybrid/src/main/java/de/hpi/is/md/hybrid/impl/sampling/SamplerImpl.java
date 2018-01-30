package de.hpi.is.md.hybrid.impl.sampling;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Sampler;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.util.IntArrayPair;
import de.hpi.is.md.util.IteratorUtils;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

@Builder
//@Metrics
final class SamplerImpl implements Sampler {

	@NonNull
	private final Iterator<int[]> left;
	@NonNull
	private final DictionaryRecords right;
	@NonNull
	private final Collection<PreprocessedColumnPair> columnPairs;
	private final boolean parallel;

	@Timed
	@Override
	public Set<SimilaritySet> processRecommendations(Collection<IntArrayPair> recommendations) {
		return StreamUtils.stream(recommendations, parallel)
			.map(this::calculateSimilaritySet)
			.collect(Collectors.toSet());
	}

	@Timed
	@Override
	public Optional<Set<SimilaritySet>> sample() {
		return IteratorUtils.next(left)
			.map(this::sample);
	}

	private SimilaritySet calculateSimilaritySet(IntArrayPair pair) {
		int[] left = pair.getLeft();
		int[] right = pair.getRight();
		return calculateSimilaritySet(left, right);
	}

	@Timed
	private SimilaritySet calculateSimilaritySet(int[] leftRecord, int[] rightRecord) {
		double[] similaritySet = StreamUtils.seq(columnPairs)
			.mapToDouble(columnPair -> columnPair.getSimilarity(leftRecord, rightRecord))
			.toArray();
		return new SimilaritySet(similaritySet);
	}

	private Set<SimilaritySet> sample(int[] leftRecord) {
		return StreamUtils.stream(right, parallel)
			.map(rightRecord -> calculateSimilaritySet(leftRecord, rightRecord))
			.collect(Collectors.toSet());
	}

}
