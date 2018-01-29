package de.hpi.is.md.util;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import java.io.File;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Reporter {

	public static void reportTo(File directory) {
		directory.mkdirs();
		MetricRegistry registry = MetricsUtils.getDefaultRegistry();
		ScheduledReporter csvReporter = CsvReporter.forRegistry(registry)
			.convertRatesTo(TimeUnit.SECONDS)
			.convertDurationsTo(TimeUnit.MILLISECONDS)
			.build(directory);
		csvReporter.report();
	}

	public static void reportTo(Logger logger) {
		MetricRegistry registry = MetricsUtils.getDefaultRegistry();
		ScheduledReporter consoleReporter = Slf4jReporter.forRegistry(registry)
			.convertRatesTo(TimeUnit.SECONDS)
			.convertDurationsTo(TimeUnit.MILLISECONDS)
			.outputTo(logger)
			.build();
		consoleReporter.report();
	}

}
