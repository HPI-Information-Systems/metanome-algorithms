package de.hpi.is.md.hybrid.impl.infer;

import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FullSpecializer {

	@NonNull
	private final FullLhsSpecializer lhsSpecializer;
	@NonNull
	private final SpecializationFilter specializationFilter;

	public Collection<MD> specialize(MDSite lhs, MDElement rhs, Int2DoubleFunction above) {
		return with(lhs, rhs).specialize(above);
	}

	private WithMD with(MDSite lhs, MDElement rhs) {
		return new WithMD(lhs, rhs);
	}

	@RequiredArgsConstructor
	private class WithMD {

		@NonNull
		private final MDSite lhs;
		private final MDElement rhs;

		private Collection<MD> specialize(Int2DoubleFunction above) {
			Iterable<MDSite> specializedLhs = specializeLhs(above);
			return StreamUtils.seq(specializedLhs)
				.filter(this::isNonTrivial)
				.map(this::initialMD)
				.toList();
		}

		private Iterable<MDSite> specializeLhs(Int2DoubleFunction above) {
			return lhsSpecializer.specialize(lhs, above);
		}

		private MD initialMD(MDSite specializedLhs) {
			MDElement rhs = initialRhs();
			return new MDImpl(specializedLhs, rhs);
		}

		private MDElement initialRhs() {
			int rhsAttr = rhs.getId();
			double threshold = rhs.getThreshold();
			return new MDElementImpl(rhsAttr, threshold);
		}

		private boolean isNonTrivial(MDSite specializedLhs) {
			return specializationFilter.filter(specializedLhs, rhs);
		}
	}
}
