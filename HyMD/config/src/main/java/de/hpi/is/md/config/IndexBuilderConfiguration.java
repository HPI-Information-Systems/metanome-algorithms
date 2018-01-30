package de.hpi.is.md.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.hybrid.impl.sim.threshold.FastSimilarityIndexBuilder;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.util.jackson.Converters;
import de.hpi.is.md.util.jackson.Entry;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;

@Data
class IndexBuilderConfiguration {

	@NonNull
	@JsonDeserialize(converter = Converter.class)
	private Map<ColumnPair<?>, SimilarityIndexBuilder> fixed = Collections.emptyMap();
	@NonNull
	@JsonProperty("default")
	private SimilarityIndexBuilder defaultIndexBuilder = FastSimilarityIndexBuilder.builder()
		.build();

	SimilarityIndexBuilder getIndexBuilder(ColumnPair<?> pair) {
		return fixed.getOrDefault(pair, defaultIndexBuilder);
	}

	private static class Converter extends
		StdConverter<Collection<Entry<ColumnPair<?>, SimilarityIndexBuilder>>, Map<ColumnPair<?>, SimilarityIndexBuilder>> {

		@Override
		public Map<ColumnPair<?>, SimilarityIndexBuilder> convert(
			Collection<Entry<ColumnPair<?>, SimilarityIndexBuilder>> value) {
			return Converters.toMap(value);
		}

	}
}
