package de.hpi.is.md.hybrid.impl.sim;

import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilderImpl.SimilarityRowBuilderFactory;
import de.hpi.is.md.util.Int2DoubleArrayRow;
import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimilarityArrayRowBuilder implements SimilarityRowBuilder {

	private final int width;

	public static SimilarityRowBuilderFactory factory() {
		return new FactoryImpl();
	}

	private static WithRow with(double[] row) {
		return new WithRow(row);
	}

	@Override
	public Int2DoubleRow create(Collection<To> similarities) {
		double[] row = new double[width];
		return with(row).process(similarities);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class WithRow {

		@NonNull
		private final double[] row;

		private void process(To to) {
			int right = to.getRight();
			double sim = to.getSimilarity();
			row[right] = sim;
		}

		private Int2DoubleRow process(Iterable<To> similarities) {
			similarities.forEach(this::process);
			return new Int2DoubleArrayRow(row);
		}

	}


	private static class FactoryImpl implements SimilarityRowBuilderFactory {

		@Override
		public SimilarityRowBuilder create(int width) {
			return new SimilarityArrayRowBuilder(width);
		}
	}
}
