package de.hpi.is.md.hybrid.impl.sim.threshold;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CollectingThresholdMapTest {

	@Parameter
	public boolean useCache;

	@Parameters
	public static Collection<Boolean> data() {
		return Arrays.asList(Boolean.TRUE, Boolean.FALSE);
	}

	@Before
	public void setUp() {
		CollectingThresholdMap.useCache(useCache);
	}

	@Test
	public void test() {
		Collection<To> row1 = Arrays.asList(
			To.builder().similarity(0.5).records(new IntOpenHashSet(Arrays.asList(Integer.valueOf(1), Integer.valueOf(2)))).build(),
			To.builder().similarity(0.6).records(IntSets.singleton(3)).build());
		Collection<To> row2 = Collections.singletonList(
			To.builder().similarity(0.4).records(IntSets.singleton(1)).build());
		ThresholdMap map = CollectingThresholdMap.builder()
			.add(1, row1)
			.add(2, row2)
			.build();
		assertThat(map.greaterOrEqual(1, 0.5)).hasSize(3);
		assertThat(map.greaterOrEqual(1, 0.5)).contains(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3));
		assertThat(map.greaterOrEqual(1, 0.6)).hasSize(1);
		assertThat(map.greaterOrEqual(1, 0.6)).contains(Integer.valueOf(3));
		assertThat(map.greaterOrEqual(1, 0.7)).isEmpty();
		assertThat(map.greaterOrEqual(2, 0.4)).hasSize(1);
		assertThat(map.greaterOrEqual(2, 0.4)).contains(Integer.valueOf(1));
	}

}