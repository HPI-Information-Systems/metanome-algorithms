package de.hpi.is.md.impl.threshold;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.util.Hasher;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@CPSType(id = "limit", base = ThresholdFilter.class)
@RequiredArgsConstructor
public class LimitSizeThresholdFilter implements ThresholdFilter {

	private final int size;
	@NonNull
	private final ThresholdFilter underlying;

	@Override
	public Iterable<Double> filter(DoubleSet similarities) {
		return LimitSizeUtils.limitSize(similarities, size);
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putClass(LimitSizeThresholdFilter.class)
			.putInt(size)
			.put(underlying);
	}

}
