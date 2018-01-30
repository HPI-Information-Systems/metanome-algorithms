package de.hpi.is.md.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.sim.impl.CrossPairGenerator;
import de.hpi.is.md.sim.impl.EqualPairGenerator;
import de.hpi.is.md.sim.impl.EqualsSimilarityMeasure;
import de.hpi.is.md.sim.impl.SimilarityComputerImpl;
import de.hpi.is.md.util.jackson.Converters;
import de.hpi.is.md.util.jackson.Entry;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;

@Data
class SimilarityComputerConfiguration {

	private static final SimilarityComputer<Object> EQUAL_COMPUTER = SimilarityComputerImpl
		.builder()
		.generator(EqualPairGenerator.INSTANCE)
		.build();
	@NonNull
	@JsonProperty("default")
	private SimilarityComputer<Object> defaultComputer = SimilarityComputerImpl
		.builder()
		.generator(CrossPairGenerator.INSTANCE)
		.build();
	@NonNull
	@JsonDeserialize(converter = Converter.class)
	private Map<ColumnPair<?>, SimilarityComputer<?>> fixed = Collections.emptyMap();

	@SuppressWarnings("unchecked")
	<T> SimilarityComputer<T> getSimilarityComputer(ColumnPair<T> pair,
		SimilarityMeasure<T> similarityMeasure) {
		SimilarityComputer<?> similarityComputer = fixed
			.getOrDefault(pair, getDefault(similarityMeasure));
		return (SimilarityComputer<T>) similarityComputer;
	}

	private <T> SimilarityComputer<Object> getDefault(SimilarityMeasure<T> similarityMeasure) {
		return similarityMeasure == EqualsSimilarityMeasure.INSTANCE ? EQUAL_COMPUTER
			: defaultComputer;
	}

	private static class Converter extends
		StdConverter<Collection<Entry<ColumnPair<?>, SimilarityComputer<?>>>, Map<ColumnPair<?>, SimilarityComputer<?>>> {

		@Override
		public Map<ColumnPair<?>, SimilarityComputer<?>> convert(
			Collection<Entry<ColumnPair<?>, SimilarityComputer<?>>> value) {
			return Converters.toMap(value);
		}

	}
}
