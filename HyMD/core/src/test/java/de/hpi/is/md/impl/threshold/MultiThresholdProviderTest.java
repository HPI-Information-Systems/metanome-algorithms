package de.hpi.is.md.impl.threshold;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import de.hpi.is.md.ThresholdProvider;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class MultiThresholdProviderTest {

	private static ThresholdProvider create(Iterable<DoubleSet> similarities) {
		return MultiThresholdProvider.create(similarities);
	}

	@Test
	public void testEmpty() {
		List<DoubleSet> similarities = Collections.singletonList(new DoubleOpenHashSet());
		ThresholdProvider provider = create(similarities);
		assertThat(provider.getNext(0, 0.0).boxed()).isEmpty();
	}

	@Test
	public void testGetNext() {
		List<DoubleSet> similarities = Arrays
			.asList(new DoubleOpenHashSet(Sets.newHashSet(0.7, 0.6)),
				new DoubleOpenHashSet(Sets.newHashSet(0.8)));
		ThresholdProvider provider = create(similarities);
		assertThat(provider.getNext(0, 0.5).boxed()).hasValue(0.6);
		assertThat(provider.getNext(0, 0.6).boxed()).hasValue(0.7);
		assertThat(provider.getNext(1, 0.8).boxed()).isEmpty();
	}

}