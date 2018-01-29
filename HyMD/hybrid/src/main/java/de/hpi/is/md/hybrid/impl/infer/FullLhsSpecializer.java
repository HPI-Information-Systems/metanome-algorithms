package de.hpi.is.md.hybrid.impl.infer;

import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.Optionals;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Seq;

@RequiredArgsConstructor
public class FullLhsSpecializer {

	@NonNull
	private final LhsSpecializer specializer;


	Iterable<MDSite> specialize(MDSite lhs, Int2DoubleFunction above) {
		return with(lhs, above).specialize();
	}

	private WithLhs with(MDSite lhs, Int2DoubleFunction above) {
		return new WithLhs(lhs, above);
	}

	@RequiredArgsConstructor
	private class WithLhs {

		@NonNull
		private final MDSite lhs;
		@NonNull
		private final Int2DoubleFunction above;

		private Iterable<MDSite> specialize() {
			int size = lhs.size();
			return Seq.range(0, size)
				.map(this::specialize)
				.flatMap(Optionals::stream)
				.toList();
		}

		private Optional<MDSite> specialize(int lhsAttr) {
			double threshold = above.applyAsDouble(lhsAttr);
			return specializer.specialize(lhs, lhsAttr, threshold);
		}
	}
}
