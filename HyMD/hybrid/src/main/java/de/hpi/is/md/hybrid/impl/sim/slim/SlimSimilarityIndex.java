package de.hpi.is.md.hybrid.impl.sim.slim;

import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.util.Int2Int2DoubleTable;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Seq;

@Builder
class SlimSimilarityIndex implements SimilarityIndex {

	private static final long serialVersionUID = 9013152487385343593L;
	@NonNull
	private final Int2Int2DoubleTable similarityTable;
	@NonNull
	private final Int2ObjectMap<IntSet> rightIndex;
	@Getter
	private final double minSimilarity;

	@Override
	public IntCollection getSimilarRecords(int leftValue, double threshold) {
		return with(threshold).getSimilarRecords(leftValue);
	}

	@Override
	public DoubleSet getSimilarities() {
		Collection<Double> values = similarityTable.values();
		return new DoubleOpenHashSet(values);
	}

	@Override
	public double getSimilarity(int leftValue, int rightValue) {
		return similarityTable.getOrDefault(leftValue, rightValue);
	}

	private WithThreshold with(double threshold) {
		return new WithThreshold(threshold);
	}

	@RequiredArgsConstructor
	private class WithThreshold {

		private final double threshold;

		private IntSet getRecords(Entry entry) {
			int id = entry.getIntKey();
			return rightIndex.get(id);
		}

		private IntCollection getSimilarRecords(int leftValue) {
			Int2DoubleMap row = similarityTable.row(leftValue).asMap();
			IntCollection result = new IntArrayList();
			Seq.seq(row.int2DoubleEntrySet())
				.filter(this::isAbove)
				.map(this::getRecords)
				.forEach(result::addAll);
			return result;
		}

		private boolean isAbove(Entry entry) {
			return entry.getDoubleValue() >= threshold;
		}
	}
}
