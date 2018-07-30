package de.hpi.is.md.hybrid.impl.sim.threshold;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;

public abstract class ThresholdMapFlattenerTest {

	@Test
	public void test() {
		Collection<To> row1 = Arrays.asList(
			To.builder().similarity(0.5).records(new IntOpenHashSet(Arrays.asList(1, 2))).build(),
			To.builder().similarity(0.6).records(IntSets.singleton(3)).build());
		Collection<To> row2 = Collections.singletonList(
			To.builder().similarity(0.4).records(IntSets.singleton(1)).build());
		ThresholdMapFlattener flattener = createFlattener(3);
		ThresholdMap map = CollectingThresholdMap.builder()
			.add(1, row1)
			.add(2, row2)
			.build(flattener);
		assertThat(map.greaterOrEqual(1, 0.5)).hasSize(3);
		assertThat(map.greaterOrEqual(1, 0.5)).contains(1, 2, 3);
		assertThat(map.greaterOrEqual(1, 0.6)).hasSize(1);
		assertThat(map.greaterOrEqual(1, 0.6)).contains(3);
		assertThat(map.greaterOrEqual(1, 0.7)).isEmpty();
		assertThat(map.greaterOrEqual(2, 0.4)).hasSize(1);
		assertThat(map.greaterOrEqual(2, 0.4)).contains(1);
	}

	protected abstract ThresholdMapFlattener createFlattener(int size);
}
