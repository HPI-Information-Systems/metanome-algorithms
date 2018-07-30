package de.hpi.is.md.impl.threshold;

import static de.hpi.is.md.impl.threshold.ThresholdFilterUtils.sorted;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.util.Hasher;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.doubles.DoubleSets;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import java.util.PrimitiveIterator.OfDouble;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@CPSType(id = "uniform", base = ThresholdFilter.class)
@RequiredArgsConstructor
public class UniformDistributionThresholdProviderThresholdFilter implements ThresholdFilter {

	private final int size;
	@NonNull
	private final ThresholdFilter underlying;

	@Override
	public Iterable<Double> filter(DoubleSet similarities) {
		if (similarities.isEmpty()) {
			return DoubleSets.EMPTY_SET;
		}
		DoubleSortedSet filtered = sorted(similarities);
		double first = filtered.firstDouble();
		int intervals = size - 1;
		if (intervals == 0) {
			return DoubleSets.singleton(first);
		}
		double last = filtered.lastDouble();
		double intervalSize = (first - last) / intervals;
		UniformTrimmer trimmer = new UniformTrimmer(intervalSize);
		OfDouble iterator = filtered.iterator();
		trimmer.trim(iterator);
		return filtered;
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putClass(UniformDistributionThresholdProviderThresholdFilter.class)
			.putInt(size)
			.put(underlying);
	}

}
