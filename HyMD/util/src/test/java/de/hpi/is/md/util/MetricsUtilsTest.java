package de.hpi.is.md.util;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import org.junit.Test;

public class MetricsUtilsTest {

	@Test
	public void testDefaultRegistry() {
		assertThat(MetricsUtils.getDefaultRegistry()).isNotNull();
	}

	@SuppressWarnings("EmptyTryBlock")
	@Test
	public void testTimer() {
		try (Context ignored = MetricsUtils.timer(MetricsUtilsTest.class, "foo")) {
		}
		Timer timer = MetricsUtils.getDefaultRegistry().timer(name(MetricsUtilsTest.class, "foo"));
		assertThat(timer.getCount()).isGreaterThanOrEqualTo(1L);
	}

}