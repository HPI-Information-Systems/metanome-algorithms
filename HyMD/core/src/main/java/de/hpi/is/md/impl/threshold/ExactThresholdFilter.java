package de.hpi.is.md.impl.threshold;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.ThresholdFilter;
import it.unimi.dsi.fastutil.doubles.DoubleSet;

@CPSType(id = "exact", base = ThresholdFilter.class)
public class ExactThresholdFilter implements ThresholdFilter {

	@Override
	public DoubleSet filter(DoubleSet similarities) {
		return similarities;
	}
}
