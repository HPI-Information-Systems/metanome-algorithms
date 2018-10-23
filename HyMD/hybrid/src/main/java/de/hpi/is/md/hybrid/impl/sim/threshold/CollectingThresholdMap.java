package de.hpi.is.md.hybrid.impl.sim.threshold;

import static it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMaps.emptyMap;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class CollectingThresholdMap implements ThresholdMap {

	private static final CacheBuilder<Object, Object> CACHE_BUILDER = CacheBuilder.newBuilder()
		.maximumSize(1_000_000L);
	private static final long serialVersionUID = -6852814144266346968L;
	@Accessors(fluent = true)
	@Setter
	private static boolean useCache = true;
	@NonNull
	private final Int2ObjectMap<Double2ObjectSortedMap<IntSet>> map;
	private transient LoadingCache<GreaterOrEqualCall, IntCollection> cache;

	static ThresholdMapBuilder builder() {
		return new ThresholdMapBuilder();
	}

	private static WithResults withResults() {
		return new WithResults();
	}

	@Override
	public IntCollection greaterOrEqual(int valueId, double max) {
		if (useCache) {
			GreaterOrEqualCall call = new GreaterOrEqualCall(valueId, max);
			LoadingCache<GreaterOrEqualCall, IntCollection> cache = getCache();
			return cache.getUnchecked(call);
		}
		return greaterOrEqual_(valueId, max);
	}

	private LoadingCache<GreaterOrEqualCall, IntCollection> createCache() {
		CacheLoader<GreaterOrEqualCall, IntCollection> loader = CacheLoader
			.from(GreaterOrEqualCall::call);
		return CACHE_BUILDER.build(loader);
	}

	private LoadingCache<GreaterOrEqualCall, IntCollection> getCache() {
		if (cache == null) {
			cache = createCache();
		}
		return cache;
	}

	private IntCollection greaterOrEqual_(int valueId, double max) {
		Double2ObjectSortedMap<IntSet> sortedMap = map.getOrDefault(valueId, emptyMap());
		return withResults().collect(sortedMap, max);
	}

	private static class WithResults {

		private final IntCollection result = new IntArrayList();

		private IntCollection collect(Double2ObjectMap<IntSet> sortedMap, double max) {
			//this function seems to be the most crucial part of the code
			for (Entry<IntSet> entry : sortedMap.double2ObjectEntrySet()) {
				double threshold = entry.getDoubleKey();
				if (threshold < max) {
					// thresholds are sorted descending
					break;
				}
				IntSet value = entry.getValue();
				result.addAll(value);
			}
			return result;
		}
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	private class GreaterOrEqualCall {

		private final int valueId;
		private final double threshold;

		private IntCollection call() {
			return greaterOrEqual_(valueId, threshold);
		}
	}


}
