package de.hpi.is.md.hybrid.impl.level;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.util.IntArrayPair;
import de.hpi.is.md.util.MetricsUtils;
import io.astefanutti.metrics.aspectj.Metrics;
import java.util.Collection;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Metrics
@Slf4j
@RequiredArgsConstructor
public class LevelWiseExecutor {

	private final Statistics statistics = new Statistics();
	@NonNull
	private final LevelStrategy levelStrategy;
	@NonNull
	private final Predicate<Statistics> evaluator;
	@NonNull
	private final CandidateProcessor processor;

	public static LevelWiseExecutorBuilder builder() {
		return new LevelWiseExecutorBuilder();
	}

	@Timed
	public boolean execute(boolean finishTraversal) {
		WithStatistics withStatistics = withStatistics();
		while (levelStrategy.areLevelsLeft()) {
			boolean inefficient = withStatistics.process();
			if (!finishTraversal && inefficient) {
				updateStatistics(withStatistics.getStatistics());
				return false;
			}
		}
		updateStatistics(withStatistics.getStatistics());
		log.debug("Finished lattice traversal");
		logStatistics();
		return true;
	}

	public Collection<IntArrayPair> pollRecommendations() {
		return processor.getRecommendations();
	}

	private void logStatistics() {
		MetricsUtils.registerGauge("mds", Integer.valueOf(statistics.getFound()));
		log.info("Found {} MDs. Validated {} MDs in {} validations. {} not supported",
			Integer.valueOf(statistics.getFound()), Integer.valueOf(statistics.getValidated()), Integer.valueOf(statistics.getGroupedValidations()),
			Integer.valueOf(statistics.getNotSupported()));
	}

	private void updateStatistics(Statistics newStatistics) {
		log.debug("Found {} MDs, deduced {} MDs. Validated {} MDs. {} invalid, {} not supported",
			Integer.valueOf(newStatistics.getFound()), Integer.valueOf(newStatistics.getNewDeduced()), Integer.valueOf(newStatistics.getValidated()),
			Integer.valueOf(newStatistics.getInvalid()), Integer.valueOf(newStatistics.getNotSupported()));
		statistics.add(newStatistics);
	}

	private WithStatistics withStatistics() {
		return new WithStatistics();
	}

	private class WithStatistics {

		@Getter
		private final Statistics statistics = new Statistics();

		private boolean process() {
			Collection<Candidate> remaining = levelStrategy.getCurrentLevel();
			if (!remaining.isEmpty()) {
				processRemaining(remaining);
				return evaluator.test(statistics);
			}
			return false;
		}

		private void processRemaining(Collection<Candidate> remaining) {
			Statistics newStatistics = processor.validateAndAnalyze(remaining);
			statistics.add(newStatistics);
			statistics.round();
		}

	}

}
