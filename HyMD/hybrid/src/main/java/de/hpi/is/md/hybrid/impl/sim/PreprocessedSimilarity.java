package de.hpi.is.md.hybrid.impl.sim;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class PreprocessedSimilarity {

	private final int left;
	private final Collection<To> similarities;
	private final double minSimilarity;

	boolean isNotEmpty() {
		return !similarities.isEmpty();
	}

	@Builder
	@Data
	public static class To {

		private final int right;
		private final double similarity;
		@NonNull
		private final IntSet records;
	}
}
