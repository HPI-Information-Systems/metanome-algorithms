package de.hpi.is.md.hybrid.impl.sim.threshold;

public class ThresholdMapHashFlattenerTest extends ThresholdMapFlattenerTest {

	@Override
	protected ThresholdMapFlattener createFlattener(int size) {
		return ThresholdMapHashFlattener.factory().create(size);
	}

}