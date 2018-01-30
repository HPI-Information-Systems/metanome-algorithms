package de.hpi.is.md.config;

import static de.hpi.is.md.util.jackson.Converters.toMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.util.jackson.Entry;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;

@Data
class MinThresholdConfiguration {

	@NonNull
	@JsonDeserialize(converter = Converter.class)
	private Map<ColumnPair<?>, Double> fixed = Collections.emptyMap();
	@JsonProperty("default")
	private double defaultMinThreshold = 0.7;

	<T> double getMinThreshold(ColumnPair<T> pair) {
		return fixed.getOrDefault(pair, defaultMinThreshold);
	}

	private static class Converter extends
		StdConverter<Collection<Entry<ColumnPair<?>, Double>>, Map<ColumnPair<?>, Double>> {

		@Override
		public Map<ColumnPair<?>, Double> convert(Collection<Entry<ColumnPair<?>, Double>> value) {
			return toMap(value);
		}

	}
}
