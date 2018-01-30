package de.hpi.is.md.demo;

import de.hpi.is.md.config.MappingConfiguration;
import de.hpi.is.md.hybrid.DiscoveryConfiguration;
import lombok.Data;
import lombok.NonNull;

@Data
class RunnerConfiguration {

	@NonNull
	private MappingConfiguration mapping = new MappingConfiguration();
	@NonNull
	private DiscoveryConfiguration discovery = new DiscoveryConfiguration();

}
