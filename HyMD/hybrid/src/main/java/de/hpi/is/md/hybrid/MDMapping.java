package de.hpi.is.md.hybrid;

import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.StreamUtils;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MDMapping implements Hashable {

	@NonNull
	private final List<ColumnConfiguration<?>> mappings;

	@Override
	public void hash(Hasher hasher) {
		hasher.putAll(mappings);
	}

	PreprocessingConfiguration getPreprocessingConfiguration() {
		List<PreprocessingColumnConfiguration<?>> configurations = mappings.stream()
			.map(ColumnConfiguration::getPreprocessingConfiguration)
			.collect(Collectors.toList());
		return new PreprocessingConfiguration(configurations);
	}

	List<ThresholdFilter> getThresholdFilters() {
		return StreamUtils.seq(mappings)
			.map(ColumnConfiguration::getThresholdFilter)
			.toList();
	}
}
