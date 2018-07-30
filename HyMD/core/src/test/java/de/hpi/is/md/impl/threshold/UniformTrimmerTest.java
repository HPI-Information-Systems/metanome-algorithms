package de.hpi.is.md.impl.threshold;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.ThresholdFilter;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class UniformTrimmerTest {

	@Test
	public void test() {
		ThresholdFilter thresholdFilter = new UniformDistributionThresholdProviderThresholdFilter(3,
			new ExactThresholdFilter());
		DoubleSet similarities = new DoubleOpenHashSet(new double[]{0.2, 0.3, 0.4, 0.5});
		Iterable<Double> thresholds = thresholdFilter.filter(similarities);
		assertThat(thresholds).hasSize(3);
		assertThat(thresholds).contains(0.2, 0.3, 0.5);
	}

	@Test
	public void testExact() {
		ThresholdFilter thresholdFilter = new UniformDistributionThresholdProviderThresholdFilter(3,
			new ExactThresholdFilter());
		DoubleSet similarities = new DoubleOpenHashSet(new double[]{0.2, 0.3, 0.4, 0.5, 0.6});
		Iterable<Double> thresholds = thresholdFilter.filter(similarities);
		assertThat(thresholds).hasSize(3);
		assertThat(thresholds).contains(0.2, 0.4, 0.6);
	}

}