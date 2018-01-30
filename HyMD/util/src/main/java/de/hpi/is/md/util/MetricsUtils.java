package de.hpi.is.md.util;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import io.astefanutti.metrics.aspectj.Metrics;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricsUtils {

	private static final String DEFAULT = "metrics-registry";

	public static MetricRegistry getDefaultRegistry() {
		MetricRegistry registry = SharedMetricRegistries.tryGetDefault();
		return Optional.ofNullable(registry)
			.orElseGet(MetricsUtils::setDefaultRegistry);
	}

	public static <T> void registerGauge(String name, T value) {
		Gauge<T> gauge = () -> value;
		getDefaultRegistry().register(name, gauge);
	}

	public static Context timer(Class<?> clazz, String name) {
		Timer timer = getDefaultRegistry()
			.timer(name(clazz, name));
		return timer.time();
	}

	private static String getDefaultRegistryName() {
		return ReflectionUtils.<String>getDefaultValue(Metrics.class, "registry")
			.orElse(DEFAULT);
	}

	private static MetricRegistry setDefaultRegistry() {
		String defaultRegistryName = getDefaultRegistryName();
		return SharedMetricRegistries.setDefault(defaultRegistryName);
	}
}
