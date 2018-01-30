package de.hpi.is.md.hybrid.impl.sim.threshold;

public class ThresholdMapArrayFlattenerTest extends ThresholdMapFlattenerTest {

	@Override
	protected ThresholdMapFlattener createFlattener(int size) {
		return ThresholdMapArrayFlattener.factory().create(size);
	}

}