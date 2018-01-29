package de.hpi.is.md.impl.threshold;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.util.Hasher;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@CPSType(id = "step", base = ThresholdFilter.class)
@RequiredArgsConstructor
class StepThresholdFilter implements ThresholdFilter {

	@NonNull
	private final Collection<Double> steps;

	@Override
	public Iterable<Double> filter(DoubleSet similarities) {
		return steps;
	}

	@Override
	public void hash(Hasher hasher) {
		hasher.putClass(StepThresholdFilter.class);
		steps.forEach(hasher::putDouble);
	}
}
