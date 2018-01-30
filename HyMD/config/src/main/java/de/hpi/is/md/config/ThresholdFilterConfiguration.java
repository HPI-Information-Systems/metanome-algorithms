package de.hpi.is.md.config;

import static de.hpi.is.md.util.jackson.Converters.toMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.impl.threshold.ExactThresholdFilter;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.util.jackson.Entry;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;

@Data
class ThresholdFilterConfiguration {

	@NonNull
	@JsonDeserialize(converter = Converter.class)
	private Map<ColumnPair<?>, ThresholdFilter> fixed = Collections.emptyMap();
	@NonNull
	@JsonProperty("default")
	private ThresholdFilter defaultThresholdFilter = new ExactThresholdFilter();

	ThresholdFilter getThresholdFilter(ColumnPair<?> pair) {
		return fixed.getOrDefault(pair, defaultThresholdFilter);
	}

	private static class Converter extends
		StdConverter<Collection<Entry<ColumnPair<?>, ThresholdFilter>>, Map<ColumnPair<?>, ThresholdFilter>> {

		@Override
		public Map<ColumnPair<?>, ThresholdFilter> convert(
			Collection<Entry<ColumnPair<?>, ThresholdFilter>> value) {
			return toMap(value);
		}

	}
}
