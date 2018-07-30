package de.hpi.is.md.impl.threshold;

import de.hpi.is.md.util.Trimmer;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class LimitSizeUtils {

	public static Iterable<Double> limitSize(DoubleCollection similarities, int size) {
		DoubleSet filtered = ThresholdFilterUtils.sorted(similarities);
		Trimmer trimmer = new Trimmer(size);
		trimmer.trim(filtered);
		return filtered;
	}
}
