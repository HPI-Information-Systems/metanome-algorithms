package de.hpi.is.md.hybrid.impl.sim.slim;

import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity;
import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.hybrid.impl.sim.SimilarityReceiver;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilder;
import de.hpi.is.md.util.Int2Int2DoubleTable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class SlimSimilarityReceiver implements SimilarityReceiver {

	private final SimilarityTableBuilder similarityTableBuilder;
	private final Int2ObjectMap<IntSet> rightIndex = new Int2ObjectOpenHashMap<>();

	@Override
	public void addSimilarity(PreprocessedSimilarity similarity) {
		int left = similarity.getLeft();
		Collection<To> similarities = similarity.getSimilarities();
		addToTable(left, similarities);
		addToRightIndex(similarities);
	}

	@Override
	public SimilarityIndex build(double minSimilarity) {
		Int2Int2DoubleTable similarityTable = similarityTableBuilder.build();
		return SlimSimilarityIndex.builder()
			.similarityTable(similarityTable)
			.rightIndex(rightIndex)
			.minSimilarity(minSimilarity)
			.build();
	}

	private void addToRightIndex(Iterable<To> similarities) {
		similarities.forEach(this::addToRightIndex);
	}

	private void addToRightIndex(To to) {
		int right = to.getRight();
		IntSet records = to.getRecords();
		rightIndex.put(right, records);
	}

	private void addToTable(int left, Collection<To> similarities) {
		similarityTableBuilder.add(left, similarities);
	}

}
