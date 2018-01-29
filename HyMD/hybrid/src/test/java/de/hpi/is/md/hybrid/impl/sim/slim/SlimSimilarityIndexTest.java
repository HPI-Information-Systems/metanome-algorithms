package de.hpi.is.md.hybrid.impl.sim.slim;

import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.hybrid.impl.sim.SimilarityIndexTest;

public class SlimSimilarityIndexTest extends SimilarityIndexTest {

	@Override
	protected SimilarityIndexBuilder createBuilder() {
		return SlimSimilarityIndexBuilder.builder().build();
	}

}