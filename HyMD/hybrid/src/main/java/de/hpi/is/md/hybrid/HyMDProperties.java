package de.hpi.is.md.hybrid;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HyMDProperties {

	public static final String SAMPLING_ENABLED = "hymd.sampling.enabled";
	public static final String TRAVERSAL_ENABLED = "hymd.traversal.enabled";

	public static boolean isSamplingEnabled() {
		return getSystemProperty(SAMPLING_ENABLED)
			.map(Boolean::parseBoolean)
			.orElse(Boolean.TRUE)
			.booleanValue();
	}

	public static boolean isTraversalEnabled() {
		return getSystemProperty(TRAVERSAL_ENABLED)
			.map(Boolean::parseBoolean)
			.orElse(Boolean.TRUE)
			.booleanValue();
	}

	private static Optional<String> getSystemProperty(String key) {
		return Optional.ofNullable(System.getProperty(key));
	}
}
