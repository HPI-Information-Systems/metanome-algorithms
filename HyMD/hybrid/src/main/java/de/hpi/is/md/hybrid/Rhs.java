package de.hpi.is.md.hybrid;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Rhs {

	private final int rhsAttr;
	private final double threshold;
	private final double lowerBound;

	@Override
	public String toString() {
		return rhsAttr + "@(" + lowerBound + "," + threshold + "]";
	}
}
