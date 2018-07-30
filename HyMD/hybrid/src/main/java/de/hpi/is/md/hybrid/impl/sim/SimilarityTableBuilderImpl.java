package de.hpi.is.md.hybrid.impl.sim;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.util.Hashable;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.Int2Int2DoubleTable;
import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimilarityTableBuilderImpl implements SimilarityTableBuilder {

	private final Int2Int2DoubleTable similarityTable;
	private final SimilarityRowBuilder rowBuilder;

	public static Factory factory(SimilarityTableFactory tableFactory,
		SimilarityRowBuilderFactory rowBuilderFactory) {
		return new FactoryImpl(tableFactory, rowBuilderFactory);
	}

	@Override
	public void add(int left, Collection<To> similarities) {
		Int2DoubleRow similarityRow = rowBuilder.create(similarities);
		similarityTable.putRow(left, similarityRow);
	}

	@Override
	public Int2Int2DoubleTable build() {
		return similarityTable;
	}

	public interface SimilarityTableFactory extends Hashable {

		Int2Int2DoubleTable create(int height);
	}

	public interface SimilarityRowBuilderFactory extends Hashable {

		SimilarityRowBuilder create(int width);
	}

	@CPSType(id = "default", base = Factory.class)
	@RequiredArgsConstructor
	private static class FactoryImpl implements Factory {

		private final SimilarityTableFactory tableFactory;
		private final SimilarityRowBuilderFactory rowBuilderFactory;

		@Override
		public SimilarityTableBuilder create(int height, int width) {
			Int2Int2DoubleTable similarityTable = tableFactory.create(height);
			SimilarityRowBuilder rowBuilder = rowBuilderFactory.create(width);
			return new SimilarityTableBuilderImpl(similarityTable, rowBuilder);
		}

		@Override
		public void hash(Hasher hasher) {
			hasher
				.putClass(FactoryImpl.class)
				.put(tableFactory)
				.put(rowBuilderFactory);
		}
	}
}
