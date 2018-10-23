package de.hpi.is.md.hybrid.impl.sim.slim;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.hybrid.impl.sim.AbstractSimilarityIndexBuilder;
import de.hpi.is.md.hybrid.impl.sim.SimilarityArrayTableFactory;
import de.hpi.is.md.hybrid.impl.sim.SimilarityMapRowBuilder;
import de.hpi.is.md.hybrid.impl.sim.SimilarityReceiver;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilder;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilderImpl;
import de.hpi.is.md.util.Hasher;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;

@CPSType(id = "slim", base = SimilarityIndexBuilder.class)
@Builder
public class SlimSimilarityIndexBuilder extends AbstractSimilarityIndexBuilder {

	@Default
	@NonNull
	private SimilarityTableBuilder.Factory tableBuilderFactory = SimilarityTableBuilderImpl
		.factory(new SimilarityArrayTableFactory(), SimilarityMapRowBuilder.factory());

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putClass(SlimSimilarityIndexBuilder.class)
			.put(tableBuilderFactory);
	}

	@Override
	public String toString() {
		return "SlimSimilarityIndexBuilder";
	}

	@Override
	protected SimilarityReceiver createReceiver(int leftSize, int rightSize) {
		SimilarityTableBuilder similarityTableBuilder = tableBuilderFactory
			.create(leftSize, rightSize);
		return new SlimSimilarityReceiver(similarityTableBuilder);
	}

}
