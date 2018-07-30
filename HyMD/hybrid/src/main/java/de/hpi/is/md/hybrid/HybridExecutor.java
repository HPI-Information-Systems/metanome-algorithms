package de.hpi.is.md.hybrid;

import static com.google.common.base.Preconditions.checkState;

import com.codahale.metrics.Timer.Context;
import de.hpi.is.md.hybrid.impl.level.LevelWiseExecutor;
import de.hpi.is.md.hybrid.impl.sampling.SamplingExecutor;
import de.hpi.is.md.util.IntArrayPair;
import de.hpi.is.md.util.MetricsUtils;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class HybridExecutor {

	@NonNull
	private final LevelWiseExecutor levelWiseExecutor;
	@NonNull
	private final SamplingExecutor samplingExecutor;

	static HybridExecutorBuilder builder(Preprocessed preprocessed) {
		return new HybridExecutorBuilder(preprocessed);
	}

	private static boolean shouldTraverse(boolean finishTraversal) {
		return HyMDProperties.isTraversalEnabled() || finishTraversal;
	}

	void discover() {
		try (Context ignored = MetricsUtils.timer(HybridExecutor.class, "discover")) {
			discover_();
		}
	}

	private void discover_() {
		checkState(HyMDProperties.isSamplingEnabled() || HyMDProperties.isTraversalEnabled(),
			"Either sampling or lattice traversal must be enabled");
		boolean done;
		do {
			boolean samplingDone = sample_();
			done = traverse_(samplingDone);
		} while (!done);
	}

	private boolean sample() {
		Collection<IntArrayPair> recommendations = levelWiseExecutor.pollRecommendations();
		return samplingExecutor.execute(recommendations);
	}

	private boolean sample_() {
		return !HyMDProperties.isSamplingEnabled() || sample();
	}

	private boolean traverse(boolean finishTraversal) {
		return levelWiseExecutor.execute(finishTraversal);
	}

	private boolean traverse_(boolean samplingDone) {
		if (shouldTraverse(samplingDone)) {
			return traverse(samplingDone);
		} else {
			return samplingDone;
		}
	}

}
