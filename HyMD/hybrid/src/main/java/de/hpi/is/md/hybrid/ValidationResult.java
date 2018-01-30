package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Collection;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Data
public class ValidationResult {

	@NonNull
	private final LhsResult lhsResult;
	@NonNull
	private final Collection<RhsResult> rhsResults;

	@Data
	public static class LhsResult {

		@NonNull
		private final MDSite lhs;
		private final long support;

	}

	@ToString
	@Builder
	public static class RhsResult {

		private final int rhsAttr;
		private final double from;
		private final double threshold;
		@NonNull
		@Getter
		private final Collection<IntArrayPair> violations;
		@Getter
		private final boolean validAndMinimal;

		public MDElement getOriginal() {
			return new MDElementImpl(rhsAttr, from);
		}

		public MDElement getActual() {
			return new MDElementImpl(rhsAttr, threshold);
		}

	}
}
