package de.hpi.is.md.sim;

import com.bakdata.util.jackson.CPSType;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.UnorderedPair;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;

@CPSType(id = "cache", base = SimilarityMeasure.class)
@Builder
public class CachedSimilarityMeasure<T> implements SimilarityMeasure<T> {

	private static final long serialVersionUID = 349046266421508632L;
	@NonNull
	private final SimilarityMeasure<T> similarityMeasure;
	@Default
	private int maximumSize = 100_000;
	private transient LoadingCache<UnorderedPair<T>, Double> cache;

	@Override
	public double calculateSimilarity(T obj1, T obj2) {
		return calculateSimilarity(UnorderedPair.of(obj1, obj2));
	}

	@Override
	public double calculateSimilarity(UnorderedPair<T> pair) {
		LoadingCache<UnorderedPair<T>, Double> cache = getCache();
		return cache.getUnchecked(pair).doubleValue();
	}

	@Override
	public void hash(Hasher hasher) {
		hasher.put(similarityMeasure);
	}

	private LoadingCache<UnorderedPair<T>, Double> createCache() {
		CacheLoader<UnorderedPair<T>, Double> loader = CacheLoader
			.from(similarityMeasure::calculateSimilarity);
		return CacheBuilder.newBuilder()
			.maximumSize(maximumSize)
			.build(loader);
	}

	private LoadingCache<UnorderedPair<T>, Double> getCache() {
		if (cache == null) {
			cache = createCache();
		}
		return cache;
	}


}
