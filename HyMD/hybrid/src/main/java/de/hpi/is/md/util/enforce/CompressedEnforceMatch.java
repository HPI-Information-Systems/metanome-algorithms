package de.hpi.is.md.util.enforce;

import lombok.Data;

@Data
public class CompressedEnforceMatch {

	private final Iterable<int[]> left;
	private final Iterable<int[]> right;

}
