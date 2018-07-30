package de.hpi.is.md.hybrid.impl.sim;

import de.hpi.is.md.hybrid.SimilarityIndex;

public interface SimilarityReceiver {

	void addSimilarity(PreprocessedSimilarity similarity);

	SimilarityIndex build(double minSimilarity);
}
