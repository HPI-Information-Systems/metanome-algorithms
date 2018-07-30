package de.hpi.is.md.hybrid.impl.sim;

import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilderImpl.SimilarityTableFactory;
import de.hpi.is.md.util.Int2Int2DoubleHashTable;
import de.hpi.is.md.util.Int2Int2DoubleTable;

public class SimilarityHashTableFactory implements SimilarityTableFactory {

	@Override
	public Int2Int2DoubleTable create(int height) {
		return Int2Int2DoubleHashTable.create(height);
	}
}
