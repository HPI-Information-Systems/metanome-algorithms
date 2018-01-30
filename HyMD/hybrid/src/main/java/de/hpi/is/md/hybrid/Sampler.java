package de.hpi.is.md.hybrid;

import de.hpi.is.md.util.IntArrayPair;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface Sampler {

	Set<SimilaritySet> processRecommendations(Collection<IntArrayPair> recommendations);

	Optional<Set<SimilaritySet>> sample();
}
