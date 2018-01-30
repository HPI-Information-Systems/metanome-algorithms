package de.hpi.is.md.hybrid;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.StreamUtils;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Seq;

@RequiredArgsConstructor
public class PreprocessingConfiguration implements Hashable {

	private final List<PreprocessingColumnConfiguration<?>> configurations;

	public List<ColumnMapping<?>> getColumnMappings() {
		return configurations.stream()
			.map(PreprocessingColumnConfiguration::getMapping)
			.collect(Collectors.toList());
	}

	@Override
	public void hash(Hasher hasher) {
		hasher.putAll(configurations);
	}

	public Seq<PreprocessingColumnConfiguration<?>> seq() {
		return StreamUtils.seq(configurations);
	}
}
