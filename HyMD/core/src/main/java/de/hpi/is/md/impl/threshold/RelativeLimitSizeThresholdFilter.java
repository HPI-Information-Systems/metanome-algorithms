package de.hpi.is.md.impl.threshold;

import static com.google.common.base.Preconditions.checkArgument;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.util.Hasher;
import de.hpi.is.md.util.MathUtils;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@CPSType(id = "relative_limit", base = ThresholdFilter.class)
@RequiredArgsConstructor
public class RelativeLimitSizeThresholdFilter implements ThresholdFilter {

	private final double sizeRatio;
	@NonNull
	private final ThresholdFilter underlying;

	@Override
	public Iterable<Double> filter(DoubleSet similarities) {
		checkArgument(0.0 <= sizeRatio && sizeRatio <= 1.0, "sizeRatio must be in [0.0, 1.0]");
		int originalSize = similarities.size();
		int size = MathUtils.roundToInt(originalSize * sizeRatio);
		return LimitSizeUtils.limitSize(similarities, size);
	}

	@Override
	public void hash(Hasher hasher) {
		hasher
			.putClass(RelativeLimitSizeThresholdFilter.class)
			.putDouble(sizeRatio)
			.put(underlying);
	}

}
