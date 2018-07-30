package de.hpi.is.md;

import lombok.Data;
import lombok.NonNull;

@Data
public class MatchingDependencyResult {

	@NonNull
	private final MatchingDependency dependency;
	private final long support;

	@Override
	public String toString() {
		return dependency + " (support=" + support + ")";
	}
}
