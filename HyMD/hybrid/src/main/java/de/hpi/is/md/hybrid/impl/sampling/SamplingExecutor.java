package de.hpi.is.md.hybrid.impl.sampling;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.Sampler;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.util.IntArrayPair;
import io.astefanutti.metrics.aspectj.Metrics;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Metrics
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SamplingExecutor {

	@NonNull
	private final Sampler sampler;
	@NonNull
	private final SimilaritySetProcessor processor;
	private final Statistics statistics = new Statistics();
	@NonNull
	private final Predicate<Statistics> evaluator;
	private final Deque<SimilaritySet> queue = new LinkedList<>();

	public static SamplingExecutorBuilder builder() {
		return new SamplingExecutorBuilder();
	}

	@Timed
	public boolean execute(Collection<IntArrayPair> recommendations) {
		WithStatistics withStatistics = withStatistics();
		withStatistics.processRecommendations(recommendations);
		boolean done = withStatistics.sample();
		updateStatistics(withStatistics.getStatistics());
		return done;
	}

	private void updateStatistics(Statistics newStatistics) {
		log.debug("Sampled {} rounds. Deduced {} MDs", newStatistics.getCount(),
			newStatistics.getNewDeduced());
		statistics.add(newStatistics);
	}

	private WithStatistics withStatistics() {
		return new WithStatistics();
	}

	//	@Metrics
	private class WithStatistics {

		@Getter
		private final Statistics statistics = new Statistics();

		private void clearQueue() {
			while (!queue.isEmpty()) {
				if (evaluator.test(statistics)) {
					break;
				}
				SimilaritySet similaritySet = queue.pollFirst();
				process(similaritySet);
			}
		}

		private void process(SimilaritySet similaritySet) {
			Statistics process = processor.process(similaritySet);
			statistics.add(process);
		}

		private void processAll(Collection<SimilaritySet> similaritySets) {
			log.debug("Processing {} recommended similarity sets", similaritySets.size());
			for (SimilaritySet similaritySet : similaritySets) {
				if (evaluator.test(statistics)) {
					break;
				}
				process(similaritySet);
			}
		}

		private void processAndQueue(Collection<SimilaritySet> similaritySets) {
			log.debug("Processing {} sampled similarity sets", similaritySets.size());
			queue.addAll(similaritySets);
			clearQueue();
		}

		@Timed
		private void processRecommendations(Collection<IntArrayPair> recommendations) {
			log.debug("Got {} recommendations", recommendations.size());
			Collection<SimilaritySet> similaritySets = sampler
				.processRecommendations(recommendations);
			processAll(similaritySets);
		}

		@Timed
		private boolean sample() {
			clearQueue();
			boolean done = sampleNew();
			return done && queue.isEmpty();
		}

		private boolean sampleNew() {
			Optional<Set<SimilaritySet>> similaritySets;
			do {
				if (evaluator.test(statistics)) {
					return false;
				}
				statistics.count();
				similaritySets = sampler.sample();
				similaritySets.ifPresent(this::processAndQueue);
			} while (similaritySets.isPresent());
			return true;
		}
	}

}
