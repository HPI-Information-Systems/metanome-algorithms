package de.hpi.is.md.hybrid.impl.sim;

import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import java.util.Collection;

public interface SimilarityRowBuilder {

	Int2DoubleRow create(Collection<To> similarities);
}
