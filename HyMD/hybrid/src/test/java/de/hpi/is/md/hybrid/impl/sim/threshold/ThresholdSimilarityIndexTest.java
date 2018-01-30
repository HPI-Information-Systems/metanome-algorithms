package de.hpi.is.md.hybrid.impl.sim.threshold;

import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.hybrid.impl.sim.SimilarityIndexTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ThresholdSimilarityIndexTest extends SimilarityIndexTest {

	@Parameter
	public SimilarityIndexBuilder indexBuilder;

	@Parameters
	public static Collection<SimilarityIndexBuilder> data() {
		return Arrays.asList(
			CollectingSimilarityIndexBuilder.builder().build(),
			FastSimilarityIndexBuilder.builder().build());
	}

	@Override
	protected SimilarityIndexBuilder createBuilder() {
		return indexBuilder;
	}

}