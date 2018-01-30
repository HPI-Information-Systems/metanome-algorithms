package de.hpi.is.md.config;

import static de.hpi.is.md.util.CastUtils.as;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.sim.impl.EqualsSimilarityMeasure;
import de.hpi.is.md.sim.impl.LevenshteinSimilarity;
import de.hpi.is.md.util.jackson.Converters;
import de.hpi.is.md.util.jackson.Entry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;

@Data
public class SimilarityMeasureConfiguration {

	@JsonProperty("default")
	@NonNull
	private SimilarityMeasure<Object> defaultMeasure = EqualsSimilarityMeasure.INSTANCE;
	@NonNull
	@JsonDeserialize(converter = Converter.class)
	private Multimap<ColumnPair<?>, SimilarityMeasure<?>> fixed = ImmutableMultimap
		.of();
	@NonNull
	private Map<Class<?>, SimilarityMeasure<?>> byType = ImmutableMap.<Class<?>, SimilarityMeasure<?>>builder()
		.put(String.class, LevenshteinSimilarity.FAST)
		.build();

	<T> Collection<SimilarityMeasure<T>> getSimilarityMeasures(ColumnPair<T> pair) {
		Collection<SimilarityMeasure<T>> fixed = getFixed(pair);
		return fixed.isEmpty() ? getByType(pair) : fixed;
	}

	@SuppressWarnings("unchecked")
	private <T> Collection<SimilarityMeasure<T>> getByType(ColumnPair<T> pair) {
		Class<T> type = pair.getType();
		SimilarityMeasure<?> byType = this.byType.getOrDefault(type, defaultMeasure);
		return Collections.singleton((SimilarityMeasure<T>) byType);
	}

	private <T> Collection<SimilarityMeasure<T>> getFixed(ColumnPair<T> pair) {
		Collection<SimilarityMeasure<?>> computer = fixed.get(pair);
		return as(computer);
	}

	private static class Converter extends
		StdConverter<List<Entry<ColumnPair<?>, SimilarityMeasure<?>>>, Multimap<ColumnPair<?>, SimilarityMeasure<?>>> {

		@Override
		public Multimap<ColumnPair<?>, SimilarityMeasure<?>> convert(
			List<Entry<ColumnPair<?>, SimilarityMeasure<?>>> value) {
			return Converters.toMultimap(value);
		}
	}
}
