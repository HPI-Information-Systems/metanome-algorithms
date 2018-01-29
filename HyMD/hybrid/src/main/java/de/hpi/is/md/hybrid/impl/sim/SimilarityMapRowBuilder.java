package de.hpi.is.md.hybrid.impl.sim;

import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilderImpl.SimilarityRowBuilderFactory;
import de.hpi.is.md.util.Int2DoubleMapRow;
import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimilarityMapRowBuilder implements SimilarityRowBuilder {

	public static SimilarityRowBuilderFactory factory() {
		return new FactoryImpl();
	}

	private static WithRow with(Int2DoubleMap row) {
		return new WithRow(row);
	}

	@Override
	public Int2DoubleRow create(Collection<To> similarities) {
		int size = similarities.size();
		Int2DoubleMap row = new Int2DoubleOpenHashMap(size);
		return with(row).process(similarities);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class WithRow {

		@NonNull
		private final Int2DoubleMap row;

		private void process(To to) {
			int right = to.getRight();
			double sim = to.getSimilarity();
			row.put(right, sim);
		}

		private Int2DoubleRow process(Iterable<To> similarities) {
			similarities.forEach(this::process);
			return new Int2DoubleMapRow(row);
		}

	}

	private static class FactoryImpl implements SimilarityRowBuilderFactory {

		@Override
		public SimilarityRowBuilder create(int width) {
			return new SimilarityMapRowBuilder();
		}
	}
}
