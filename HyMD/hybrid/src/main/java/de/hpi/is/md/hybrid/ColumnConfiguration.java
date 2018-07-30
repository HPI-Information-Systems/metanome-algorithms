package de.hpi.is.md.hybrid;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import lombok.Data;
import lombok.NonNull;

@Data
public class ColumnConfiguration<T> implements Hashable {

	@NonNull
	private final ThresholdFilter thresholdFilter;
	@NonNull
	private final PreprocessingColumnConfiguration<T> preprocessingConfiguration;

	@Override
	public void hash(Hasher hasher) {
		ColumnMapping<T> mapping = preprocessingConfiguration.getMapping();
		hasher
			.put(mapping)
			.put(thresholdFilter);
	}

	@Override
	public String toString() {
		return preprocessingConfiguration.toString();
	}
}
