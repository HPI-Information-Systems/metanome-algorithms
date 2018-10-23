package de.hpi.is.md.hybrid.impl.sim.threshold;

import com.bakdata.util.jackson.CPSType;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.hybrid.impl.sim.AbstractSimilarityIndexBuilder;
import de.hpi.is.md.hybrid.impl.sim.SimilarityArrayTableFactory;
import de.hpi.is.md.hybrid.impl.sim.SimilarityMapRowBuilder;
import de.hpi.is.md.hybrid.impl.sim.SimilarityReceiver;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilder;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilderImpl;
import de.hpi.is.md.hybrid.impl.sim.threshold.ThresholdMapFlattener.Factory;
import de.hpi.is.md.util.Hasher;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Setter;

@CPSType(id = "fast", base = SimilarityIndexBuilder.class)
@Setter
@Builder
public final class FastSimilarityIndexBuilder extends AbstractSimilarityIndexBuilder {

	private static final Factory DEFAULT_FLATTENER_FACTORY = ThresholdMapArrayFlattener
		.factory();
	private static final SimilarityTableBuilder.Factory DEFAULT_TABLE_BUILDER_FACTORY = SimilarityTableBuilderImpl
		.factory(new SimilarityArrayTableFactory(), SimilarityMapRowBuilder.factory());
	@Default
	@NonNull
	private ThresholdMapFlattener.Factory flattenerFactory = DEFAULT_FLATTENER_FACTORY;
	@Default
	@NonNull
	private SimilarityTableBuilder.Factory tableBuilderFactory = DEFAULT_TABLE_BUILDER_FACTORY;
	@JsonCreator
	private FastSimilarityIndexBuilder() {
		this.flattenerFactory = DEFAULT_FLATTENER_FACTORY;
		this.tableBuilderFactory = DEFAULT_TABLE_BUILDER_FACTORY;
	}

	private FastSimilarityIndexBuilder(
		Factory flattenerFactory,
		SimilarityTableBuilder.Factory tableBuilderFactory) {
		this.flattenerFactory = flattenerFactory;
		this.tableBuilderFactory = tableBuilderFactory;
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putClass(FastSimilarityIndexBuilder.class)
			.put(flattenerFactory)
			.put(tableBuilderFactory);
	}

	@Override
	public String toString() {
		return "FastSimilarityIndexBuilder";
	}

	@Override
	protected SimilarityReceiver createReceiver(int leftSize, int rightSize) {
		SimilarityTableBuilder similarityTableBuilder = tableBuilderFactory
			.create(leftSize, rightSize);
		ThresholdMapFlattener flattener = flattenerFactory.create(leftSize);
		return new FastSimilarityReceiver(similarityTableBuilder, flattener);
	}

}
