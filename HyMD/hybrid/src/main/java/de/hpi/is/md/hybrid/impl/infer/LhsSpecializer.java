package de.hpi.is.md.hybrid.impl.infer;

import de.hpi.is.md.ThresholdProvider;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LhsSpecializer {

	@NonNull
	private final ThresholdProvider thresholdProvider;

	public Optional<MDSite> specialize(MDSite lhs, int attr, double threshold) {
		return with(lhs).specialize(attr, threshold);
	}

	private WithLhs with(MDSite lhs) {
		return new WithLhs(lhs);
	}

	private final class WithLhs extends LhsModifier {

		private WithLhs(MDSite lhs) {
			super(lhs);
		}

		private Optional<MDSite> specialize(int attr, double threshold) {
			return thresholdProvider.getNext(attr, threshold)
				.map(next -> newLhs(attr, next));
		}
	}

}
