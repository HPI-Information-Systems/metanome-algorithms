package de.hpi.is.md.impl.threshold;

import static com.google.common.base.Preconditions.checkElementIndex;

import de.hpi.is.md.ThresholdProvider;
import de.hpi.is.md.util.OptionalDouble;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import java.util.List;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Seq;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiThresholdProvider implements ThresholdProvider {

	@NonNull
	private final SingleThresholdProvider[] providers;

	public static ThresholdProvider create(Iterable<? extends Iterable<Double>> thresholds) {
		SingleThresholdProvider[] providers = StreamUtils.seq(thresholds)
			.map(SingleThresholdProvider::of)
			.toArray(SingleThresholdProvider[]::new);
		return new MultiThresholdProvider(providers);
	}

	@Override
	public List<DoubleSortedSet> getAll() {
		return Seq.of(providers)
			.map(SingleThresholdProvider::getAll)
			.toList();
	}

	@Override
	public OptionalDouble getNext(int attr, double threshold) {
		checkElementIndex(attr, providers.length);
		return providers[attr].getNext(threshold);
	}

}
