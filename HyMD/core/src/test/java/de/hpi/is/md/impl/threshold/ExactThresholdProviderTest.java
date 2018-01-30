package de.hpi.is.md.impl.threshold;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import de.hpi.is.md.ThresholdFilter;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import org.junit.Test;

public class ExactThresholdProviderTest {

	@Test
	public void testEmpty() {
		ThresholdFilter thresholdFilter = new ExactThresholdFilter();
		Iterable<Double> thresholds = thresholdFilter.filter(new DoubleOpenHashSet());
		assertThat(thresholds).isEmpty();
	}

	@Test
	public void testGetNext() {
		ThresholdFilter thresholdFilter = new ExactThresholdFilter();
		Iterable<Double> thresholds = thresholdFilter
			.filter(new DoubleOpenHashSet(Sets.newHashSet(0.7, 0.6)));
		assertThat(thresholds).hasSize(2);
		assertThat(thresholds).contains(0.7, 0.6);
	}

}