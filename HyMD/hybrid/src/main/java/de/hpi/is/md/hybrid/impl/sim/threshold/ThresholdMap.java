package de.hpi.is.md.hybrid.impl.sim.threshold;

import it.unimi.dsi.fastutil.ints.IntCollection;
import java.io.Serializable;

public interface ThresholdMap extends Serializable {

	IntCollection greaterOrEqual(int valueId, double max);
}
