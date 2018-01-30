package de.hpi.is.md.util;

import lombok.Data;
import lombok.NonNull;

@Data
public class IntArrayPair {

	@NonNull
	private final int[] left;
	@NonNull
	private final int[] right;
}
